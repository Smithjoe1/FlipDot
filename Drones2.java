import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Duncan
 * Date: 2/04/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class Drones2 extends Animation implements Observer{
    static final int NUM_DRONES = 2000;
    List<Drone> drones = new ArrayList<Drone>(NUM_DRONES);

    boolean free=false;  //when this becomes false, the particles move toward their goals

    float pAccel=.2f;  //acceleration rate of the particles
    float pMaxSpeed=2;  //max speed the particles can move at




    PApplet parent;


    PImage attractor;

    Drones2(Flipdot p){
        parent = p;
        attractor = p.loadImage("data/BuzzLogoStatic.gif");
        attractor.loadPixels();
        createDrones(attractor.get());
        p.addObserver(this);
    }

    void createDrones(PImage pg){
        for(int i=0; i < pg.width; i++){
            for(int j = 0; j < pg.height; j++){
                pg.loadPixels();
                if(pg.get(i,j) != color(255)){
                    drones.add(new Drone(i, j));
                }
            }
        }
    }

    void resetAnimation(){
        for (Drone drone : drones) {
            drone.x = parent.random(xbound);
            drone.y = parent.random(ybound);
        }
    }
    PGraphics drawAnimation(PGraphics pg) {
        pg.loadPixels();
        pg.stroke(255);
        pg.strokeWeight(1);

        pg.beginDraw();
        pg.background(0);
        for (Drone drone : drones) {
            pg.point(drone.x, drone.y);
        }
        pg.endDraw();
        return pg;
    }

    void updateAnimation(){
        for (Drone drone : drones) {
            drone.update();
        }
    }

    class Drone{
        float x,y;
        float xVel, yVel;
        float xAccel, yAccel;
        float xVelMax, yVelMax;
        float ang;

        float xGoal, yGoal;

        Drone(float newXGoal, float newYGoal){
            x = parent.random(newXGoal-50,newXGoal+50);
            y = parent.random(newYGoal-50,newYGoal+50);
            xGoal = newXGoal;
            yGoal = newYGoal;
            ang = random(0,TWO_PI);
            xVel = 0;
            yVel = 0;

            xAccel = pAccel * cos(ang);
            yAccel = pAccel * sin(ang);
        }

        void update(){
            //println(degrees(ang));
            x+=xVel;
            y+=yVel;

            xVel+=xAccel;
            yVel+=yAccel;

            //if the particles are not free, move this particle toward its goal
            if (!free){
                //get the ange of the thing point the particle should move toward
                ang= atan2(yGoal-y, xGoal-x);
                xAccel=pAccel*cos(ang);
                yAccel=pAccel*sin(ang);

                //slow it down a lot if it's close to the point
                if (abs(x-xGoal) <abs(xVel*2) || abs(y-yGoal) <abs(yVel*2) ){
                    xVel*=0.6;
                    yVel*=0.6;
                }
            }else{
                //make it bounce off edges if it's free moving
//                CheckEdge();
            }
        }
        void CheckEdge(){
            if(x<xbound*-0.5){
                x = 0;
                xAccel*=-1;
            } else if(x>xbound*1.5){
                x = xbound;
                xAccel*=-1;
            }
            if(y<ybound*-0.5){
                y = 0;
                yAccel*=-1;
            }else if(y>ybound*1.5){
                y = ybound;
                yAccel*=-1;
            }
        }
    }

    @Override
    public void update(char keyCode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
