package VisibleHistory.playerdeath;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.TinyChest;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

import static com.megacrit.cardcrawl.helpers.ImageMaster.CAMPFIRE_SMITH_BUTTON;

public class DeadPlayer {
    public float x;
    public float y;
    public Texture img;
    public boolean flipHorizontal=false;
    public boolean flipVertical=false;
    public Hitbox hb;

    public DeadPlayer(float x,float y,Texture img){
        this.x=x;
        this.y=y;
        this.img=img;
        this.hb=new Hitbox(300,150);
        this.hb.x=this.x+100;
        this.hb.y=this.y;

    }
    public void update() {
        this.hb.update();
if (this.hb.hovered){

}

  }
    public void render(SpriteBatch sb) {
this.hb.render(sb);
        if (!this.hb.hovered){
            sb.setColor(1.0f, 1.0f, 1.0f, 0.35f);
        }else {
            sb.setColor(Color.WHITE);
        }
        sb.draw(this.img, this.x , this.y);
        sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
//todo 做一个虚化+高亮显示
        // FontHelper.renderFont(sb,FontHelper.largeCardFont,"测试位置",this.x,this.y, Color.WHITE);
    }
    public static ArrayList<DeadPlayer> deadPlayers=new ArrayList<>();
}
