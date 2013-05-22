import processing.core.PGraphics;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Duncan
 * Date: 18/03/13
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Snowflakes extends Animation {
//    Particle[] particles = new Particle[0];
    ArrayList<Particle> particles = new ArrayList<Particle>();

    int maxParticles = 100;
    int height;

    Snowflakes(Flipdot p){
        for(int i = 0; i < maxParticles; i++){
            particles.add(new Particle(random(0,200),-20));
        }
        height = p.height;
    }

    //This renders the animation
    PGraphics drawAnimation(PGraphics pg) {
        pg.beginDraw();
        pg.background(0);
        pg.noStroke();
        for(int i = 0; i < particles.size(); i++){
             pg.ellipse(particles.get(i).x, particles.get(i).y, particles.get(i).partSize, particles.get(i).partSize);
        }
        pg.endDraw();
        return pg;
    }

    //This updates the animation
    void updateAnimation(){
//        particles.add(new Particle(random(0,200),-20));
//        particles = (Particle[]) append(particles, new Particle(random(0,200),-20));

        for(int i = 0; i < particles.size(); i++){
            if (particles.get(i).y > height + 10) {
                particles.remove(i);//Lets remove the particle if it's off screen
                particles.add(new Particle(random(0,200),-20));
            }

            particles.get(i).x += particles.get(i).xVel;
            particles.get(i).y += particles.get(i).yVel;
            particles.get(i).updateVelocity();
        }
    }

    //This resets the animation
    void resetAnimation(){
        particles.clear();
        for(int i = 0; i < maxParticles; i++){
            particles.add(new Particle(random(0,200),-20));
        }
    }

    class Particle{
        float x;
        float y;
        float xVel;
        float yVel;
        float partSize;

        Particle(float xPos, float yPos){
            x = xPos;
            y = yPos;
            xVel = random(-1, 2);  //Random speed
            yVel = random(0, 2);   //Speed the snow falls
            partSize = random(2, 5);
        }

        void updateVelocity(){
            xVel = map(noise(x, y),0,1,-3,3);
            if(xVel < 0.2) xVel = 0;
        }
    }
}
