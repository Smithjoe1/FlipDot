import processing.core.PApplet;

/**
 * Created with IntelliJ IDEA.
 * User: Duncan
 * Date: 13/03/13
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnimationFactory {

    Animation animation = null;
    PApplet parent;

     AnimationFactory(){
     }

    Animation createGif(Flipdot p, String strLines){
        return new Images(p, strLines);
    }

    Animation createTetris(Flipdot p){
        return new TetrisClock(p);
    }

    Animation createDrones(Flipdot p){
        return new Drones(p);
    }

    Animation createDrones2(Flipdot p){
        return new Drones2(p);
    }

    Animation createMatrix(Flipdot p){
        return new Matrix(p);
    }

    Animation createSnow(Flipdot p){
        return new Snowflakes(p);
    }
}
