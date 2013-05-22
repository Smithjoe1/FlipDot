/**
 Observer class from http://stackoverflow.com/questions/5131547/java-keylistener-in-separate-class
 */

import processing.core.*;
//import processing.serial.*;

import java.io.File;
import java.util.*;
import g4p_controls.*;
//import processing.serial.Serial;

public class Flipdot extends PApplet implements Observabe{

    Fader f;
    int Dmode;

//    GSketchPad draw;

    PGraphics pg;
    PGraphics pgIN;

    public ImageProcessor imgPr;

    AnimationFactory af = new AnimationFactory();

    boolean animationBlock = true;
    FrameSaver saver = new FrameSaver(this);

    // sample set of faders to pick from at random
    Fader[] faders=new Fader[]{
           //new RotateBoxFader(this),
            new RightFade(this),
            new RotateFade(this),
            new EllipseFade(this),
            new EllipseFadePoint(this),
            new EllipseFadePointReverse(this),
//            new CrossFade(this),     //Wont work
            new DownFade(this),
            new LeftFade(this),
            new RandomBlockFade(this),
            new SanfuFade(this),
            new SnakeFade(this),
            new SnakeFadeRight(this),
            new waveFade(this),
    };

    ArrayList<Animation> images = new ArrayList<Animation>();
    ArrayList<String[]> config = new ArrayList<String[]>();

    //========================================================================
    //Transisiton Stuff
    float delay = 1.0f*5; //How long until the transition happens
    float speed = 100.0f; //How long will the transition takes, in frames (5/sec)

    //This is the number of panels high
    int partHeight = 14;  // Number of pixels high per panel
    int partWidth = 28;   // Number of pixels wide per panel

    int panelx = 7;
    int panely = 4;
    int xbound = panelx*partWidth;
    int ybound = panely*partHeight;

    int currImg = 0;
    int nextImg = 2;
    int carousel = 0;

    int scale = 5;
    int framerate = 5;

    int validX, validY;


    static public void main(String args[]){
        PApplet.main(new String[] {"Flipdot"});
    }

    public void setup() {
        size(xbound*scale, ybound*scale);
        ybound = panely*partHeight;

        pg = createGraphics(xbound, ybound);
        pgIN = createGraphics(xbound, ybound);
//        draw = new GSketchPad(this, 0, 0, xbound*scale, ybound*scale);
        noSmooth();
//        draw.setGraphic(pg);

        frameRate(framerate);

        f=faders[(int)(random(0,faders.length))];

        loadConfig();

        f.setXY(120, 30);
        reset();
//        saver.start();
        imgPr.start();
    }

    public void draw(){
        background(250);
        pg.beginDraw();
        pgIN.beginDraw();

        images.get(currImg).updateAnimation();
        images.get(nextImg).updateAnimation();
        images.get(currImg).drawAnimation(pg); //Draw the animation routine
        images.get(nextImg).drawAnimation(pgIN);
        f.setImages(pg.get(), pgIN.get());

        if(frameCount > delay){
            float percent = ((frameCount-delay)*framerate)/speed;
            if (percent<=1) {
                // show next frame of the animation

                blockAnimationCheck(percent);
                //Draw blocked animations to flush the buffer
                images.get(currImg).updateAnimation();
                images.get(nextImg).updateAnimation();
                images.get(currImg).drawAnimation(pg);
                images.get(nextImg).drawAnimation(pgIN);
                f.setImages(pg.get(), pgIN.get());
                f.animateFade(percent);
            } else {
                // show end image
                f.endFade();
                reset();

            }
        } else {
            f.startFade();
        }

        //Buffer the animation once per draw cycle


//        pg = imgPr.drawSplit();
        pg.filter(THRESHOLD);
        pg = f.draw(pg);
        pg.loadPixels();
        pg.filter(THRESHOLD);
        imgPr.bufferImg(pg);
        pg.endDraw();
      pgIN.endDraw();
        image(pg,0,0,xbound*scale, ybound*scale);

//        saver.addImage(pg.get());
//        saver.run();
    }

    void loadConfig(){
        String[] strValue;
        String[] screenRes;
        String[] strLines = loadStrings("config.txt");
        strValue = split(strLines[0], "=");
        //Load serial port configuration
//        println(Serial.list());
        try{
            imgPr = new ImageProcessor((strValue[2]),this);
        }catch(UnsatisfiedLinkError e){
            println(e);
        }catch(RuntimeException e){
            println(e);
        }

        //Load the screen resolution
        if (strValue.length > 0) {
            screenRes = split(strValue[1], "x");
            panelx = Integer.parseInt(screenRes[0]);//7 //This is the number of panels wide
            panely = Integer.parseInt(screenRes[1]);//4
        }
        //Create animation objects
        images.add(af.createTetris(this));
        images.add(af.createDrones(this));
        images.add(af.createDrones2(this));
        images.add(af.createMatrix(this));
        images.add(af.createSnow(this));
        //Load the list of files
        //TODO: Test this works, creates an array list for each line in the config file and then loads the file.
        for (int i = 1; i < strLines.length; i++) {
            try {
                images.add(af.createGif(this, strLines[i]));
//                print(i+3 + " :");
//                println(strLines[i]);
            } catch (NullPointerException e) {
                println(strLines[i]);
            }
        }

        //Load the slideshow images into the caourosel
        loadSlideshow();

    }

    //Slideshow stuff
    void loadSlideshow(){
        String[] strValue;

        String[] strLines = loadStrings("Slideshow.txt");
        for (int i = 0; i < strLines.length; i++) {
            try {
                strValue = split(strLines[i], '\t');
                config.add(strValue);
            }catch (NullPointerException e) {
                println(strLines[i]);
            }
        }
    }


     public void blockAnimationCheck(float percent){ //Method to block animations while transitioning if they have problems with the draw before render stuff
         //Lets reset all the animations the first time the loop is called.
         if(animationBlock == true){
             for(int i = 0; i < images.size()-1; i++){
                 //We wanto to reset everything asides the outbound animation
                 if(i != nextImg)images.get(i).resetAnimation();
                 if(i != nextImg)images.get(i).updateAnimation();
             }
             animationBlock = false;
         }


         //Custom animation blocks for circle animations
         //Lets start the animations early otherwise we get an ugly pause before the image and animations load.
         int img;
         if(carousel == 0){
             img = 0;
         }else{
             img = carousel-1;
         }
         if(Integer.parseInt(config.get(img)[1]) == 2){
             if(percent<0.25f){
                 images.get(currImg).resetAnimation();
             }
         }
         else if(Integer.parseInt(config.get(img)[1]) >= 3 && Integer.parseInt(config.get(img)[1]) <= 4){
             if(percent<0.7f){
                 images.get(currImg).resetAnimation();
             }
         }
         //Lets do the same for the Sanfu and Sname animations, blocking the outbound animations otherwise the visual is too noisy
          else if(Integer.parseInt(config.get(img)[1]) >= 8 && Integer.parseInt(config.get(img)[1]) <= 10){
             if(percent<1){
                 //Lets block both animations completley
                 images.get(currImg).resetAnimation();
                 images.get(nextImg).loadAnimation();
             }
         }

         //Everything else
         else{
             if(currImg==0){//Tetris clock
                 //Bugs out when not on screen, resulting in incomplete letters or are already drawn, want to see the inital animation
                 //So we will block the animation from rendering until the fade is complete. (possibly half complete)
                 if(percent<0.2f){
                    images.get(currImg).resetAnimation();
                    println("Blocking tetrisclock");
                 }
                 println(percent);
             }   else if(currImg == 1){  //Drones
                     //Doesn't look right when not animating and transitioning.
             }  else {
                 //Everything else
                if(percent<0.9f){
                     images.get(nextImg).pauseAnimation();
                } else { //Make sure we unpause our animations once the transition is complete
                    images.get(nextImg).resetAnimation();
                }

                if(currImg >= 21 && percent<=0.9f) { //If the animation isn't a city outbound, then stop it from looping
                     images.get(currImg).resetAnimation();  //This should be forcing the animation to stick to the 1st frame and reset all it's timer variables
                     images.get(nextImg).loadAnimation();//Keep the outbound animation stuck on the last frame
                }
                 if( currImg >= 21 && percent >= 0.9f){
                     images.get(nextImg).resetAnimation();
                 }
             }


         }

     }

    public void reset() {
//        println("Reset");
        frameCount=0;
        animationBlock = true;
        try{


            f = faders[Integer.parseInt(config.get(carousel)[1])];
            nextImg = Integer.parseInt(config.get(carousel)[0]);
            if (1 + carousel <= config.size()-1)
                currImg = Integer.parseInt(config.get(carousel + 1)[0]);  //Wrap if greater than the arrayList
            else currImg = Integer.parseInt(config.get(0)[0]);


            images.get(currImg).drawAnimation(pg);
            images.get(nextImg).drawAnimation(pgIN);
//            println("Carolsel is :"+carousel);
//            println("CurrImg is :"+currImg);
//            println("NextImg is :"+nextImg);

            f.setImages(pg, pgIN);//Draw the gif routine
            f.startFade();

            speed = Integer.parseInt(config.get(carousel)[2]);
            delay = Float.parseFloat(config.get(carousel)[3]);

            if(Integer.parseInt(config.get(carousel)[4])==0){ //See if the current frame has a XY value, if it doesn;t, load a safe value
                if(carousel + 1 < config.size()){
                    if(Integer.parseInt(config.get(carousel+1)[4])!=0) { //See if the next frame has an XYvalue
                        f.setXY(Integer.parseInt(config.get(carousel+1)[4]),Integer.parseInt(config.get(carousel+1)[5]));
                    } else {f.setXY(validX, validY);}
                }
            }else{ //If the curent frame has an XY value, load it and set the safe value.
                f.setXY(Integer.parseInt(config.get(carousel)[4]),Integer.parseInt(config.get(carousel)[5]));
                validX = Integer.parseInt(config.get(carousel)[4]);
                validY = Integer.parseInt(config.get(carousel)[5]);
            }

//            images.get(currImg).resetAnimation();
//            images.get(nextImg).resetAnimation();

            if(carousel < config.size()-1){
                carousel++;
            } else {
                carousel = 0;
                println("looping");
            }
        }catch(NumberFormatException e){
            println(e);
        }
    }



    //Observers
    private ArrayList<Observer> obsList = new ArrayList();

    public void keyPressed(){
        notifyObservers(key);
    }
    public void keyReleased(){}
    public void keyTyped(){}

    public void notifyObservers(char KeyCode){
        for(Observer obs: obsList){
            obs.update(key);
        }
    }
    //Setters and Getters
    public void addObserver(Observer obs){
        if(obs != null)
            obsList.add(obs);
    }

    public void delObserver(Observer obs){
        if (obs != null)
            obsList.remove(obs);
    }


    //Setters and getters
    int getXbound(){return xbound;}
    int getYbound(){return ybound;}

    int getpartWidth(){return partWidth;}
    int getpartHeight(){return partHeight;}
}
