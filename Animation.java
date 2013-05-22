import processing.core.PApplet;
import processing.core.PGraphics;

class Animation extends Flipdot{

    PApplet parent;

    Animation(Flipdot p){
        parent = p;
    }

    Animation() {
    }

    //This renders the animation
    PGraphics drawAnimation(PGraphics pg) { return pg; }

    //This updates the animation
    void updateAnimation(){}

    //This resets the animation
    void resetAnimation(){}

    void loadAnimation(){}

    void pauseAnimation(){}
}
