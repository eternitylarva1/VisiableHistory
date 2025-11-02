package VisibleHistory.playerdeath;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class DeadPlayer {
    public float x;
    public float y;
    public Texture img;
    public DeadPlayer(float x,float y,Texture img){
        this.x=x;
        this.y=y;
        this.img=img;
    }
    public void render(SpriteBatch sb) {

    }
    public static ArrayList<DeadPlayer> deadPlayers=new ArrayList<>();
}
