import GameObjects.Bullets;
import GameObjects.Collectibles.PowerUp;
import GameObjects.Collectibles.PowerUpGenerator;
import GameObjects.Enemies.Enemy;
import GameObjects.Enemies.EnemyGenerator;
import GameObjects.Lifes;
import GameObjects.Score;
import GameObjects.SpaceShip;
import org.academiadecodigo.simplegraphics.graphics.Color;
import org.academiadecodigo.simplegraphics.graphics.Rectangle;
import org.academiadecodigo.simplegraphics.keyboard.Keyboard;
import org.academiadecodigo.simplegraphics.keyboard.KeyboardEvent;
import org.academiadecodigo.simplegraphics.keyboard.KeyboardEventType;
import org.academiadecodigo.simplegraphics.keyboard.KeyboardHandler;
import org.academiadecodigo.simplegraphics.pictures.Picture;

import java.util.LinkedList;

public class Game implements KeyboardHandler {


    private Player playerOne;
    private Player playerTwo;
    private boolean playing = true;
    private boolean paused = false;
    private LinkedList<Bullets> friendlyBullets = new LinkedList<>();
    private LinkedList<Bullets> enemyBullets = new LinkedList<>();
    private LinkedList<PowerUp> powerUps = new LinkedList<>();

    // TODO: 11/02/2019 MAKE A SHIP linkedlist
    private LinkedList<SpaceShip> spaceShips = new LinkedList<>();


    private Rectangle rect = new Rectangle(10, 10, 800, 800);
    private Picture menu = new Picture(10, 10, "menu_spaceneon_400x400.png");
    // TODO: 11/02/2019 change simple graphics to take more fonts and possibly more input keys

    // REFACTOR
    private Score score = new Score();
    private STATE state = STATE.MENU;
    private LinkedList<Enemy> enemies = new LinkedList<>();
    private TopBar topBar = new TopBar("top_bar_800x40.png");
    private BottomBar bottomBar = new BottomBar("bottom_bar_800x40.png");
    private EnemyGenerator enemyGenerator = new EnemyGenerator(enemies, enemyBullets);
    private PowerUpGenerator powerUpGenerator = new PowerUpGenerator(powerUps);
    private FramesPerSecond fps = new FramesPerSecond();

    enum STATE {
        MENU,
        GAME
    }


    public Game() {

        spaceShips.add(new SpaceShip(250, 700, friendlyBullets, "spaceship_blue_30x30.png", "bullet_blue_20x30.png", 50));
        spaceShips.add(new SpaceShip(500, 700, friendlyBullets, "green_spaceship_30x30.png", "bullet_green_20x30.png", 730));


        // TODO: 11/02/2019 MOVE THIS - dont create players before the ships
        playerOne = new Player(KeyboardEvent.KEY_UP, KeyboardEvent.KEY_DOWN, KeyboardEvent.KEY_LEFT, KeyboardEvent.KEY_RIGHT, KeyboardEvent.KEY_SPACE, spaceShips.get(0));
        playerTwo = new Player(KeyboardEvent.KEY_W, KeyboardEvent.KEY_S, KeyboardEvent.KEY_A, KeyboardEvent.KEY_D, KeyboardEvent.KEY_T, spaceShips.get(1));


    }

    /**
     * Initializes window
     */
    public void init() {

        // TODO: 11/02/2019 Add mouse events and other game essentials for menu etc

        // Keyboard
        Keyboard k = new Keyboard(this);

        // Keyboard events
        KeyboardEvent PAUSE = new KeyboardEvent();
        PAUSE.setKey(KeyboardEvent.KEY_P);
        PAUSE.setKeyboardEventType(KeyboardEventType.KEY_PRESSED);
        k.addEventListener(PAUSE);

        KeyboardEvent START = new KeyboardEvent();
        START.setKey(KeyboardEvent.KEY_L);
        START.setKeyboardEventType(KeyboardEventType.KEY_PRESSED);
        k.addEventListener(START);
    }


    /**
     * Starts the game
     */
    public void start() {

        init();

        long initialTime = System.nanoTime();
        final double amountOfTicks = 60.0;
        double numberOfSeconds = 1000000000 / amountOfTicks;
        double delta = 0;

        long timer = System.currentTimeMillis();
        int frames = 0;

        while (playing) {


            long now = System.nanoTime();
            delta += (now - initialTime) / numberOfSeconds;
            initialTime = now;

            if (delta >= 1) {

                if (!paused) {

                    tick();
                    render();
                }
                frames++;
                delta--;
            }


            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                //System.out.println(updates + " FPS");
                fps.setFps(frames);
                frames = 0;
            }
        }
    }


    /**
     * Responsable for calling every GameObject to action
     */
    private void tick() {

        if (state == STATE.MENU) {
            return;
        }

        enemyGenerator.tick();
        powerUpGenerator.tick();
        score.tick();


        //////////////////////////////////////////////////////////////////////////////////////////////

        // SPACESHIPS WITH ENEMIES AND ENEMY BULLETS
        // TODO: 11/02/2019 This will be an array
        for (int i = 0; i < spaceShips.size(); i++) {
            spaceShips.get(i).tick();

            // WITH ENEMIES
            for (int j = 0; j < enemies.size(); j++) {
                if (spaceShips.get(i).getHitbox().intersects(enemies.get(j).getHitbox())) {

                    spaceShips.get(i).hit();
                    enemies.get(j).hit(20);
                    enemies.remove(enemies.get(j));

                    if (spaceShips.get(i).getHp() <= 0) {
                        spaceShips.remove(spaceShips.get(i));
                    }

                    //if it collides with one leave the for loop
                    j = enemies.size();
                }
            }
        }


        //////////////////////////////////////////////////////////////////////////////////////////////

        // Friendly bullets out of bounds and collision with enemies
        for (int i = 0; i < friendlyBullets.size(); i++) {

            friendlyBullets.get(i).tick();

            if (friendlyBullets.get(i).getImgY() <= 40) {
                friendlyBullets.get(i).bulletImage.delete();
                friendlyBullets.remove(friendlyBullets.get(i));
                i--;
                continue;
            }

            for (int j = 0; j < enemies.size(); j++) {

                if (friendlyBullets.get(i).getHitbox().intersects(enemies.get(j).getHitbox())) {

                    friendlyBullets.get(i).hit();
                    friendlyBullets.remove(friendlyBullets.get(i));
                    i--;

                    // TODO: 12/02/2019 adicionar dano as balas?
                    enemies.get(j).hit();

                    if (enemies.get(j).getHp() <= 0) {
                        enemies.remove(enemies.get(j));
                        score.setScore(50);
                        j--;
                    }
                    //end this loop
                    j = enemies.size();
                }
            }

        }

        ///////////////////////////////////////////////////////////////////////////////////////


        // Enemy out of bounds
        for (int i = 0; i < enemies.size(); i++) {

            enemies.get(i).tick();

            // Out of bounds
            if (enemies.get(i).getEnemyImage().getY() >= 750) {
                enemies.get(i).getEnemyImage().delete();
                enemies.remove(enemies.get(i));

                // TODO: 11/02/2019 just added
                if (enemies.size() > 1) {
                    i--;
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////


        //enemyBullets out of bounds and collision with spaceships
        for (int i = 0; i < enemyBullets.size(); i++) {

            enemyBullets.get(i).tick();

            if (enemyBullets.get(i).getImgY() >= 745) {

                enemyBullets.get(i).bulletImage.delete();
                enemyBullets.remove(enemyBullets.get(i));
                // TODO: 11/02/2019 ver isto!!!!
                i--;
                continue;
            }

            for (int j = 0; j < spaceShips.size(); j++) {

                if (spaceShips.get(j).getHitbox().intersects(enemyBullets.get(i).getHitbox())) {

                    spaceShips.get(j).hit();
                    enemyBullets.get(i).hit();
                    enemyBullets.remove(enemyBullets.get(i));

                    if (spaceShips.get(j).getHp() <= 0) {
                        spaceShips.remove(spaceShips.get(j));
                    }

                    //if it collides with one leave the for loop
                    i--;
                    j = spaceShips.size();
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////

        //powerUps out of bounds and collision with spaceships
        for (int i = 0; i < powerUps.size(); i++) {

            powerUps.get(i).tick();

            if (powerUps.get(i).getImgY() >= 745) {

                powerUps.get(i).powerUpImage.delete();
                powerUps.remove(powerUps.get(i));
                // TODO: 11/02/2019 ver isto!!!!
                i--;
                continue;
            }

            for (int j = 0; j < spaceShips.size(); j++) {

                if (spaceShips.get(j).getHitbox().intersects(powerUps.get(i).getHitbox())) {


                    spaceShips.get(j).powerUp(powerUps.get(i).getPowerUpType());
                    powerUps.get(i).hit();
                    powerUps.remove(powerUps.get(i));

                    //if it collides with one leave the for loop
                    i--;
                    j = spaceShips.size();
                }
            }

        }
    }


    /**
     * Responsable for rendering everything to the screen
     */
    private void render() {

        if (state == STATE.MENU) {

            menu.draw();
            return;

        }




        for (int i = 0; i < spaceShips.size(); i++) {
            spaceShips.get(i).render();
        }


        for (Enemy enemy : enemies) {
            enemy.render();
        }

        for (int i = 0; i < powerUps.size(); i++) {

            powerUps.get(i).render();
        }

        for (int i = 0; i < enemyBullets.size(); i++) {

            enemyBullets.get(i).render();
        }

        for (int i = 0; i < friendlyBullets.size(); i++) {

            friendlyBullets.get(i).render();
        }

        // TODO: 11/02/2019 fix this bug
        topBar.render();
        bottomBar.render();
        fps.render();
        score.render();


    }

    @Override
    public void keyPressed(KeyboardEvent keyboardEvent) {

        // TODO: 11/02/2019 specify in what game state this controls exist


        // IF STATE = GAME
        if (keyboardEvent.getKey() == 80) {
            System.out.println(paused);
            paused = !paused;
        }

        //IF STATE = MENU
        if (keyboardEvent.getKey() == 76)
            state = STATE.GAME;
        menu.delete();
        rect.setColor(Color.BLACK);
        rect.fill();
    }


    @Override
    public void keyReleased(KeyboardEvent keyboardEvent) {

    }
}
