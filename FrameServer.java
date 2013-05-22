import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.util.ArrayList;

class FrameSaver extends Thread {

    CircularArrayList<PImage> imgstore;

    int savecnt=0;
    int index=0;

    boolean running;

    PApplet parent;

    public FrameSaver (PApplet p) {
        running = false;
        parent = p;
        imgstore = new CircularArrayList<PImage>(500);
    }

    public void start() {
        parent.println("recording frames!");
        running = true;

        try{
            super.start();
        }
        catch(java.lang.IllegalThreadStateException itse){
            parent.println("cannot execute! ->" + itse);
        }
    }

    public void addImage(PImage img){
        index ++;
        try{
            imgstore.add(img);
        } catch(OutOfMemoryError e){
            parent.println(e);
        }
    }

    public void run(){
        while(running){
            if(imgstore.size()>1){
                saveIncremental(imgstore.get(0),"imageSequence/Flipdot","bmp");
                imgstore.remove(0);
            }
        }
    }

    public void quit() {
        parent.println("stopped recording..");
        running = false;
        interrupt();
    }

    // Saves a file with automatically incrementing filename,
// so that existing files are not overwritten. Will
// function correctly for less than 1000 files.

    void saveIncremental(PImage img,String prefix,String extension) {

        boolean ok=false;
        String filename="";
        File f;

        while(!ok) {
            // Get a correctly configured filename
            filename=prefix;
            if(savecnt<10) filename+="0000";
            else if(savecnt<100) filename+="000";
            else if(savecnt<1000) filename+="00";
            else if(savecnt<10000) filename+="0";
            filename+=""+savecnt+"."+extension;

            // Check to see if file exists, using the undocumented
            // savePath() to find sketch folder
            f=new File(parent.savePath(filename));
            if(!f.exists()) ok=true; // File doesn't exist
            savecnt++;
        }

        parent.println("Saving "+filename);
        try{
        img.save(filename);
        }catch(NullPointerException e){
            parent.println(e);
            parent.println(imgstore.size());
        }
    }
}

