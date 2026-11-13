package com.shadowquest.world;

import com.shadowquest.core.Game;
import com.shadowquest.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import static com.shadowquest.world.EnvironmentObject.Kind.*;

class ForestCollisionTest {
    @Test
    void forestIsRepeatableInsideWorldAndSpawnIsClear() {
        GameWorld world = MysticForest.create();
        assertEquals(2880, world.getWidth());
        assertEquals(1800, world.getHeight());
        assertEquals(world.getObjects(), MysticForest.create().getObjects());
        for (var kind : EnvironmentObject.Kind.values()) {
            assertTrue(world.getObjects().stream().anyMatch(o -> o.kind() == kind));
        }
        for (var object : world.getObjects()) {
            assertTrue(object.x() >= 0 && object.y() >= 0);
            assertTrue(object.x()+object.width() <= world.getWidth());
            assertTrue(object.y()+object.height() <= world.getHeight());
            if (object.isSolid()) assertFalse(overlaps(0, 0, 24, object.hitbox()));
        }
        assertEquals(new EnvironmentObject(TREE,190,80,180,230), world.getObjects().get(76));
        Game game = new Game();
        assertFalse(game.getWorld().getObjects().isEmpty());
    }

    @Test
    void worldCopiesAndProtectsObjectList() {
        var list = new ArrayList<EnvironmentObject>();
        list.add(new EnvironmentObject(GRASS,10,10,20,20));
        GameWorld world = new GameWorld(400,400,list);
        list.clear();
        assertEquals(1,world.getObjects().size());
        assertThrows(UnsupportedOperationException.class, () -> world.getObjects().clear());
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(10,10,world.getObjects()));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentObject(TREE,Double.NaN,0,20,20));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentObject(ROCK,0,0,0,20));
    }

    @ParameterizedTest
    @EnumSource(value=EnvironmentObject.Kind.class, names={"TREE","ROCK","LARGE_ROOT"})
    void solidHitboxesAreSmallerThanArtAndBlockAllFourDirections(EnvironmentObject.Kind kind) {
        var object = new EnvironmentObject(kind,200,200,100,100);
        var b = object.hitbox();
        assertTrue(object.isSolid());
        assertTrue(b.width() < object.width() && b.height() < object.height());
        GameWorld world = new GameWorld(600,600,List.of(object));
        assertEquals(b.x()-24, CollisionManager.resolve(world,0,b.y(),550,b.y(),24).x());
        assertEquals(b.right(), CollisionManager.resolve(world,500,b.y(),0,b.y(),24).x());
        assertEquals(b.y()-24, CollisionManager.resolve(world,b.x(),0,b.x(),550,24).y());
        assertEquals(b.bottom(), CollisionManager.resolve(world,b.x(),500,b.x(),0,24).y());
    }

    @ParameterizedTest
    @EnumSource(value=EnvironmentObject.Kind.class, names={"BUSH","GRASS","PATH","MIST","FIREFLY","SMALL_ROOT"})
    void decorationsAreWalkable(EnvironmentObject.Kind kind) {
        var object = new EnvironmentObject(kind,100,0,100,100);
        assertFalse(object.isSolid());
        assertThrows(IllegalStateException.class, object::hitbox);
        Game game = new Game(new GameWorld(500,500,List.of(object)));
        game.startNewGame();
        game.movePlayer(1,0,1);
        assertEquals(200,game.getPlayer().getX());
    }

    @Test
    void canopyBranchesAndSmallRootAreaDoNotBlock() {
        var tree = new EnvironmentObject(TREE,100,100,200,240);
        var world = new GameWorld(600,600,List.of(tree));
        var result = CollisionManager.resolve(world,0,110,500,110,24);
        assertEquals(500,result.x());
        assertEquals(110,result.y());
        assertTrue(tree.hitbox().y() > 110+24);
    }

    @ParameterizedTest
    @EnumSource(value=EnvironmentObject.Kind.class, names={"TREE","ROCK","LARGE_ROOT"})
    void sessionStopsRepeatedPressureAndAllowsDeparture(EnvironmentObject.Kind kind) {
        var obstacle = new EnvironmentObject(kind,200,0,100,100);
        var box = obstacle.hitbox();
        Game game = new Game(new GameWorld(600,500,List.of(obstacle)));
        game.startNewGame();
        game.movePlayer(0,1,box.y()/200);
        game.movePlayer(1,0,10);
        assertEquals(box.x()-24,game.getPlayer().getX());
        for(int i=0;i<100;i++) game.movePlayer(1,0,.02);
        assertEquals(box.x()-24,game.getPlayer().getX());
        game.movePlayer(-1,0,.25);
        assertEquals(box.x()-24-50,game.getPlayer().getX(),1e-9);
        game.movePlayer(0,0,1);
        assertEquals(box.x()-24-50,game.getPlayer().getX(),1e-9);
    }

    @ParameterizedTest
    @CsvSource({"1,1,100,100,500,500", "-1,1,500,100,100,500",
            "1,-1,100,500,500,100", "-1,-1,500,500,100,100"})
    void diagonalSweepsCannotEndInsideOrSkipAlignedObstacle(int dx,int dy,double x,double y,double tx,double ty) {
        var obstacle = new EnvironmentObject(ROCK,250,250,100,100);
        var world = new GameWorld(700,700,List.of(obstacle));
        // Aligned on X first: the swept Y segment must stop even if its endpoint is beyond the rock.
        var result = CollisionManager.resolve(world,obstacle.hitbox().x(),y,
                obstacle.hitbox().x(),ty,24);
        assertFalse(overlaps(result.x(),result.y(),24,obstacle.hitbox()));
        assertEquals(dy>0 ? obstacle.hitbox().y()-24 : obstacle.hitbox().bottom(),result.y());
        var diagonal = CollisionManager.resolve(world,x,y,tx,ty,24);
        assertFalse(overlaps(diagonal.x(),diagonal.y(),24,obstacle.hitbox()));
        assertTrue(diagonal.x() != tx || diagonal.y() != ty);
    }

    @ParameterizedTest
    @CsvSource({"1,0", "-1,0", "0,1", "0,-1", "1,1", "-1,1", "1,-1", "-1,-1"})
    void repeatedFractionalPressureCannotEnterThickRoot(int dx,int dy) {
        var root = new EnvironmentObject(LARGE_ROOT,540,310,150,52);
        var b = root.hitbox();
        GameWorld world = new GameWorld(1200,1000,List.of(root));
        double x = dx > 0 ? b.x()-24-1.37 : dx < 0 ? b.right()+1.37 : b.x()+20;
        double y = dy > 0 ? b.y()-24-1.37 : dy < 0 ? b.bottom()+1.37 : b.y()+5;
        double length = Math.hypot(dx,dy);
        for(int frame=0;frame<500;frame++) {
            var p = CollisionManager.resolve(world,x,y,x+dx/length*3.333333333,
                    y+dy/length*3.333333333,24);
            assertFalse(overlaps(p.x(),p.y(),24,b));
            if(frame == 0) assertTrue(p.x() != x+dx/length*3.333333333
                    || p.y() != y+dy/length*3.333333333);
            x=p.x(); y=p.y();
        }
    }

    @Test
    void diagonalSlidesAtWallWithoutBoostAndIsFrameIndependentAlongWall() {
        var obstacle = new EnvironmentObject(LARGE_ROOT,200,0,100,1000);
        var world = new GameWorld(1200,1200,List.of(obstacle));
        Player single = new Player("Arin");
        Player split = new Player("Arin");
        single.move(0,1,2.25);
        split.move(0,1,2.25);
        single.move(1,1,1.5,world);
        for(int i=0;i<150;i++) split.move(1,1,.01,world);
        assertEquals(obstacle.hitbox().x()-24,single.getX());
        assertEquals(single.getX(),split.getX(),1e-8);
        assertEquals(single.getY(),split.getY(),1e-8);
        assertEquals(450+300/Math.sqrt(2),single.getY(),1e-8);
    }

    @Test
    void nearestObstacleWinsRegardlessOfListOrderAndExactTouchCanMoveAway() {
        var near = new EnvironmentObject(ROCK,100,0,50,100);
        var far = new EnvironmentObject(ROCK,300,0,50,100);
        var world = new GameWorld(600,600,List.of(far,near));
        double edge = near.hitbox().x()-24;
        assertEquals(edge,CollisionManager.resolve(world,0,30,550,30,24).x());
        assertEquals(edge-10,CollisionManager.resolve(world,edge,30,edge-10,30,24).x());
    }

    @Test
    void cameraAndWorldEdgesWorkWithObstacles() {
        GameWorld world = new GameWorld(2880,1800,List.of(new EnvironmentObject(ROCK,200,100,80,60)));
        Player p = new Player("Arin");
        p.move(1,0,100,world);
        p.move(0,1,100,world);
        assertEquals(2856,p.getX()); assertEquals(1776,p.getY());
        Camera camera = new Camera(world);
        camera.follow(p.getX(),p.getY(),960,550);
        assertEquals(936,camera.worldToScreenX(p.getX()));
        assertEquals(526,camera.worldToScreenY(p.getY()));
        p.move(-1,0,100,world); p.move(0,-1,100,world);
        assertEquals(0,p.getX()); assertEquals(0,p.getY());
    }

    private static boolean overlaps(double x,double y,double size,EnvironmentObject.Hitbox b) {
        return x < b.right() && x+size > b.x() && y < b.bottom() && y+size > b.y();
    }
}
