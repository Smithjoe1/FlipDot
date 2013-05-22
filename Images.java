import gifAnimation.Gif;
import processing.core.*;

public class Images extends Animation{
    PImage[] image;
    Gif animation;

    Images(Flipdot p, String file){
        animation = new Gif(this, file);
        animation.loop(); //Set the animation to loop over and over
       // animation
    }

    PGraphics drawAnimation(PGraphics pg) {
        pg.image(animation,0,0);
        return pg;
    }

    void updateAnimation(){
        animation.run();
    }

    void resetAnimation(){
        animation.play();

    }

    void loadAnimation(){
        if(animation.getNumFrames()>1){
            animation.jump(animation.getNumFrames()-1);
        } else {
            animation.jump(0);
        }
    }

    void pauseAnimation(){
        animation.pause();
    }
}
