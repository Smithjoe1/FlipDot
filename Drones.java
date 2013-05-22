import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Duncan
 * Date: 10/02/13
 * Time: 1:21 PM
 * To change this template use File | Settings | File Templates.
 */

class Drones extends Animation implements Observer{
    static final int NUM_DRONES = 2000;
    List<Drone> drones = new ArrayList<Drone>(NUM_DRONES);
    static float TWO_PI = (float)Math.PI * 2f;

    float t;
    float dt;



    PImage attractor;

    Wanderer wanderer;
    PApplet parent;

    Drones(Flipdot p){
        parent = p;
        attractor = p.loadImage("data/BuzzLogoStatic.gif");
        attractor.loadPixels();
        createDrones(attractor.get());
        wanderer = new Wanderer(parent, parent.random(xbound), parent.random(ybound));
        p.addObserver(this);
    }

    void createDrones(PImage pg){
        for(int i=0; i < pg.width; i++){
            for(int j = 0; j < pg.height; j++){
                pg.loadPixels();
                if(pg.get(i,j) != color(255)){
                    drones.add(new Drone(parent, parent.random(i-5,i+5), parent.random(j-5,j+5), i, j, parent.random(TWO_PI)));
                }
            }
        }
//        for (int i = 0; i < NUM_DRONES; i++){
//            drones.add(new Drone(parent, parent.random(xbound), parent.random(ybound), parent.random(TWO_PI)));
//        }
        t = parent.millis() * (float)1e-3;
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
        dt = -t;
        t = parent.millis() * (float)1e-4;
        dt += t;
        wanderer.stayInsideCanvas();
        wanderer.move();
        for (Drone drone : drones) {
            drone.update(dt);
        }
    }

    public void update(char key){

    }


    class Drone {
        PApplet parent;

        // Speed limits
        final float MIN_SPEED = 50;
        final float MAX_SPEED = 100;

        // Acceleration and turning speed maximums
        final float MAX_ACCEL = 50;
        final float MAX_TURN = TWO_PI;

        // Ranges used to determine behaviour mode
        static final float OUTER_RANGE = 300;
        static final float INNER_RANGE = 2;

        // Enumerated behaviour modes
        static final int MODE_ATTACK = 0;
        static final int MODE_EVADE = 1;

        public int Dmode = 0;

        float xGoal, yGoal; //Where does the point want to go when in the right mode.

        float x, y;   // position (in pixel coordinates)
        float theta;  // direction
        float speed;
        float phi;
        int mode;

        Drone(PApplet p, float x, float y, float xGoal, float yGoal, float theta) {
            this.x = x;
            this.y = y;
            this.xGoal = xGoal;
            this.yGoal = yGoal;
            this.theta = theta;
            this.speed = MIN_SPEED;
            this.mode = MODE_ATTACK;
            parent = p;
        }

        void update(float dt) {

            // Modify mode based on distance from cursor
            float s = parent.dist(x, y, wanderer.getX() - x, wanderer.getY());


            if(Dmode == 0){
                switch (mode) {
                    case MODE_ATTACK:
                        if (parent.random(s) < parent.random(INNER_RANGE))
                            mode = MODE_EVADE;
                        break;
                    case MODE_EVADE:
                        if (parent.random(s) > parent.random(OUTER_RANGE))
                            mode = MODE_ATTACK;
                        break;
                    default:
                        return;
                }
            }

            // Calculate accel and turn based on set mode.

            if(Dmode == 0){
                phi = (float)(Math.atan2(wanderer.getY() - y, wanderer.getX() - x) - theta);

            }else{
                phi = (float)(Math.atan2(yGoal - y, xGoal - x)-theta);
            }
            float accel, turn;
            if(Dmode == 0){
                switch (mode) {
                    case MODE_ATTACK:
                        accel = (float)Math.cos(phi) * MAX_ACCEL;
                        turn = (float)Math.sin(phi) * MAX_TURN;
                        break;
                    case MODE_EVADE:
                        accel = (float)Math.sin(phi) * MAX_ACCEL;
                        turn = (float)Math.cos(phi) * MAX_TURN;
                        break;
                    default:
                        return;
                }
            }else{
                accel = (float)Math.cos(phi) * MAX_ACCEL/10;
                turn = (float)Math.sin(phi) * MAX_TURN;


        }
            // Recalculate speed, keeping it between the predefined limits
            float k = 0;
            if(Dmode == 0){
                if (accel > 0)
                    k = (MAX_SPEED - speed) / (MAX_SPEED - MIN_SPEED);
                else
                    k = (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
            }else{
                if(parent.dist(x, y, xGoal - x, yGoal) < 5){
                    k = (MAX_SPEED/10 - speed) / (MAX_SPEED - MIN_SPEED);
                } else{
                    k = (speed - MIN_SPEED) / (MAX_SPEED/10 - MIN_SPEED);
                }
            }
            speed += k * accel;

            // Update direction
            theta = (theta + turn * dt) % TWO_PI;

            // Update position
            x += speed * dt * Math.cos(theta);
            y += speed * dt * Math.sin(theta);

            //Check for extended bounds
            if(x<xbound*-0.5){
                x = 0;
            } else if(x>xbound*1.5){
                x = xbound;
            }
            if(y<ybound*-0.5){
                y = 0;
            }else if(y>ybound*1.5){
                y = ybound;
            }
        }
    }

    class Wanderer {
        PApplet parent;

        float x;
        float y;
        float wander_theta;
        float wander_radius;

        // bigger = more edgier, hectic
        float max_wander_offset = .30f;
        // bigger = faster turns
        float max_wander_radius = 3.5f;

        Wanderer(PApplet p, float _x, float _y)
        {
            parent = p;
            x = _x;
            y = _y;

            wander_theta = parent.random(TWO_PI);
            wander_radius = parent.random(max_wander_radius);
        }

        void stayInsideCanvas()
        {
            if (x<5)
                wander_theta = wander_theta * -1;
            if (x>xbound-5)
                wander_theta = wander_theta * -1;
            if (y<5)
                wander_theta = wander_theta * -1;
            if (y> ybound-5)
                wander_theta = wander_theta * -1;
            if (x<=0)
                x=xbound-3;
            if (y<=0)
                y=ybound-3;

            x %= xbound;
            y %= ybound;
        }

        void move()
        {
            float wander_offset = parent.random(-max_wander_offset, max_wander_offset);
            wander_theta += wander_offset;

            x += Math.cos(wander_theta);
            y += Math.sin(wander_theta);
            parent.ellipse(getX(),getY(),2,2);
        }

        float getX()
        {
            return x;
        }

        float getY()
        {
            return y;
        }
    }
}

