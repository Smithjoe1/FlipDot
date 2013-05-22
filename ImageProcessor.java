import processing.core.*;
//import processing.serial.*;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageProcessor extends Thread {
    int maxBytes = 112;
    byte[] bytes = new byte[maxBytes];
    CircularArrayList<byte[]> outputBuffer;
    PImage buff;
    boolean running;
    boolean addingFrame;
    String serPort;
//    CloseThread serCloser = new CloseThread();

    PGraphics buffer;
    Flipdot parent;
    SerialPort port;
    int xcount;
    int ycount;
    PImage[] r;
    ByteArrayOutputStream outputStream;

    ImageProcessor(String Port, Flipdot p){
        parent = p;
        initSer(Port);
        buffer = parent.createGraphics(p.getXbound(),p.getYbound());
        xcount = (int)Math.ceil(p.getXbound()/(p.getpartWidth() + 0.0)); //Number of panels wide from the total pixel width
        ycount = (int)Math.ceil(p.getYbound()/(p.getpartHeight() + 0.0)); // Number of panels high from the total pixel height
        r = new PImage[xcount*ycount];
        outputBuffer = new CircularArrayList<byte[]>(1000); //Lets create a 100 frame output buffer
    }

    public void start() {
        parent.println("Starting data steam!");
        running = true;
        try{
            if(!port.isOpened())initSer(serPort);
            super.start();

        }
        catch(java.lang.IllegalThreadStateException itse){
            parent.println("cannot execute! ->" + itse);
        }
    }

    public void run(){
        if(outputBuffer.size()>100)System.out.println(outputBuffer.size());// Print if the buffer gets too big
        while(running){
            while(!addingFrame){
                if(outputBuffer.size()>1){
                    try{
                        sendData(outputBuffer.get(0));
//                        parent.println("sending data");
                        outputBuffer.remove(0);
                    }catch(IllegalStateException e){
                        outputBuffer.clear();//Lets flush the buffer if we have any problems
                        parent.println("Trying to reinit serial");
                        quit();
                        start();
                        parent.println(e);
                    }
                }

            }
        }
    }

    public void quit() {
        parent.println("stopped sending data..");
        closePort();
        running = false;
        interrupt();
    }

    void initSer(String _port){
            parent.println("Opening port");
            try{
//                parent.println(port.);
                port = new SerialPort(_port);
                try{
                    port.openPort();
                    port.setParams(57600, 8, 1, 0);
                }catch(SerialPortException e){
                    System.out.println(e);
                }

                serPort = _port;
                System.out.println(port.toString());
                System.out.print("Is Opened?:"); System.out.println(port.isOpened());
                for (int i=0; i < maxBytes; i++) bytes[i] = parent.parseByte(0);
            } catch (RuntimeException e){
                e.printStackTrace();
            }
        }




    void closePort(){
        try{
            port.closePort();
        }catch(SerialPortException e){System.out.println(e);}
//        initSer(serPort);
    }

    void bufferImg(PGraphics pg){
        addingFrame = true;
        buff = parent.createImage(parent.getXbound(), parent.getYbound(), parent.ARGB);
        pg.loadPixels();
        buff.loadPixels();
        parent.arrayCopy(pg.pixels, buff.pixels);
        buff.updatePixels();
        r = splitImage(buff);
        for(int i = 0; i < r.length; i++){
            convertToByte(r[i],i);
            try{
                outputBuffer.add(outputStream.toByteArray());
            }catch(Exception e){
                outputBuffer.clear();
                parent.print("Adding frame: ");parent.println(addingFrame);
                parent.print("Running: ");parent.println(running);
                parent.println("Trying to reinit serial");
                quit();
                start();
                parent.println(e);
            }
//            sendData();
        } finishFrame();
        outputBuffer.add(outputStream.toByteArray()); //Lets add the a byte array of the output stream to our buffer.  //Does this need to be called twice, or does it get captured when the loop runs?
        addingFrame = false;
//        sendData();
    }

    PImage[]  splitImage(PImage originalImg){
        PImage[] output = new PImage[xcount*ycount];
        int index = 0;

        for (int ax = 0; ax < xcount; ax ++) { // Shifting panel width
            for (int ay = 0; ay < ycount; ay ++) { // Shifting panel height

                //Create image at coordinate (ax, ay) from the top left of the original image
                output[index] = parent.createImage(parent.getpartWidth(), parent.getpartHeight(), parent.ALPHA);
                output[index].loadPixels();
                originalImg.loadPixels();
                //Loop to copy pixels for each chunk
                for (int px = 0; px < parent.getpartWidth(); px ++){
                    for (int py = 0; py < parent.getpartHeight(); py ++ ){
                        int newLoc = px + py * parent.getpartWidth(); // Create a 2d location of the array
                        int oldLoc = (ax * parent.getpartHeight() + px) + ((ay * parent.getpartHeight()+py)*parent.getXbound())  ;
                        output[index].pixels[newLoc] = originalImg.pixels[oldLoc];
                    }
                }
                index++;
            }
        }
        return output;
    }

    PGraphics drawSplit(){
        buffer = parent.createGraphics(parent.getXbound(),parent.getYbound());
        buffer.beginDraw();
        int index = 0;
        for (int dy = 0; dy < ycount; dy++) {
            for (int dx = 0; dx < xcount; dx++) {
                r[index].loadPixels();
//                buffer.image(r[index], dx * partWidth, dy * partHeight, partWidth, partHeight);
                buffer.set( dx * parent.getpartWidth(), dy * parent.getpartHeight(), r[index]);
                index ++;
            }
        }
        buffer.endDraw();
//        buffer.loadPixels();
        return buffer;
    }

    void convertToByte(PImage frame, int address){
        int index = 0;
        StringBuilder byteBuff = new StringBuilder();
        String[] Str = new String[2048]; //String for Stirngbuffer debugging
        byte[] data = new byte[112];

        outputStream = new ByteArrayOutputStream();  //Flush the outputStream
        outputStream.write(0x80); //Header   ("0x80 ");
        outputStream.write(0x84);   //Command Byte  ("0x84");
        outputStream.write(address);                     //Address    (hex(address,2));
        for (int py = 0; py < parent.getpartHeight(); py ++ ){
            for (int px = 0; px < parent.getpartWidth(); px ++){
                int newLoc = px + py * parent.partWidth; // Create a 2d location of the array
                //Convert the row of pixels into a string for byte conversion
                //There are 7 bits before the byte resets
                //We check the last bit, then append another byte onto the end for downstream processing.
                //println((frame.pixels[newLoc]));

                if (byteBuff.length() == 7)
                //When the array is at it's 7th bit, append a 0 to the end and reverse the string direction
                //Then convert the string from binary to an int and then into a byte
                {
                    byteBuff.append("0");
                    byteBuff.reverse();

                    data[index] = parent.parseByte(parent.unbinary(byteBuff.toString()));
                    byteBuff.delete(0,byteBuff.length());
                    //print(hex(data[index])); //print the raw hex string as it's being created
                    outputStream.write(data[index]); //We append the data array to the output stream
                    index++;
                    //println("pop!");
                } else {
//                    parent.println(frame.pixels[newLoc]);
                    { if (frame.pixels[newLoc] != -1)    //Here we can test the value of the pixel, to determine if it's black or white
                    {
                        byteBuff.append("1");
                    } else {
                        byteBuff.append("0");
                    }
                    }
                }
            }
        }
        //println(" hex");
        outputStream.write(0x8F);

        //println();
        // println(" 0x84");
    }

    void sendData(byte[] byteArray) {
        //Prepare the outputbytestream so it's ready to be sent
//       =outputStream.toByteArray(); //Convert the outputbytestream to a byte array to be sent down the serial port.
        try{
            port.writeBytes(byteArray);
        }catch(SerialPortException e){
            System.out.println(e);
        }
    }

    void finishFrame(){
        outputStream = new ByteArrayOutputStream();//Flush the outputStream
        //Refresh the entire panel
        outputStream.write(0x80);
        outputStream.write(0x82);
        outputStream.write(0x8F);
    }
}

