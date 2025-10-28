package tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import main.GamePanel;

public class TileManager {
    GamePanel gp;
    public ArrayList<Tile> tile = new ArrayList<>();
    public int[][] mapTileNum;

    // map raw codes (as strings) so other managers can read special codes (hazards,
    // etc.)
    public String[][] mapCodes;

    // Sprite Bank
    public BufferedImage[] boxSprites;
    public Map<String, BufferedImage[]> playableSprites = new HashMap<>();
    public BufferedImage enemyPlaceholder;

    public ArrayList<int[]> boxPositions = new ArrayList<>();
    public ArrayList<int[]> playableCharacterPositions = new ArrayList<>();
    public ArrayList<int[]> enemyPositions = new ArrayList<>();

    public TileManager(GamePanel gp) {
        this.gp = gp;

        // Provide default values if gp is null (e.g., menu panels)
        int cols = (gp != null) ? gp.maxScreenCol : 16;
        int rows = (gp != null) ? gp.maxScreenRow : 12;
        int tileSize = (gp != null) ? gp.tileSize : 48;

        mapTileNum = new int[cols][rows];
        mapCodes = new String[cols][rows];

        loadTileImages();
        loadBoxSprites();
        loadPlayableSprites();
        loadEnemyPlaceholder(tileSize);

        // Only load map if gp != null (in-game context)
        if (gp != null && gp.currentMapPath != null) {
            loadMap(gp.currentMapPath);
            validateMap();
        }
    }

    // ------------------------------------------------------------------------
    private void loadBoxSprites() {
        String[] files = { "box1.png", "box2.png", "box3.png", "box4.png" };
        boxSprites = new BufferedImage[files.length];
        for (int i = 0; i < files.length; i++) {
            try (InputStream is = getClass().getResourceAsStream("/Resource/tiles/" + files[i])) {
                if (is == null) {
                    System.err.println("Missing box sprites: " + files[i]);
                    continue;
                }
                boxSprites[i] = ImageIO.read(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPlayableSprites() {
        String[] roles = { "knight", "thief", "wizard" };
        for (String role : roles) {
            BufferedImage[] sprites = new BufferedImage[6];
            try {
                String base = "/Resource/playableCharacter/" + role + "/";
                sprites[0] = ImageIO.read(getClass().getResourceAsStream(base + role + "_L_1.png"));
                sprites[1] = ImageIO.read(getClass().getResourceAsStream(base + role + "_L_2.png"));
                sprites[2] = ImageIO.read(getClass().getResourceAsStream(base + role + "_L_3.png"));
                sprites[3] = ImageIO.read(getClass().getResourceAsStream(base + role + "_R_1.png"));
                sprites[4] = ImageIO.read(getClass().getResourceAsStream(base + role + "_R_2.png"));
                sprites[5] = ImageIO.read(getClass().getResourceAsStream(base + role + "_R_3.png"));
            } catch (IOException e) {
                System.err.println("Error loading sprites for " + role);
                e.printStackTrace();
            }
            playableSprites.put(role, sprites);
        }
    }

    private void loadEnemyPlaceholder(int size) {
        enemyPlaceholder = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = enemyPlaceholder.createGraphics();
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, size, size);
        g2.dispose();
    }

    public void loadTileImages() {
        String[][] tiles = {
                // id, path, collision
                { "0", "/Resource/tiles/grass1.png", "false" },
                { "1", "/Resource/tiles/grass2.png", "false" },
                { "2", "/Resource/tiles/wall1.png", "true" },
                { "3", "/Resource/tiles/wall2.png", "true" },
                { "4", "/Resource/tiles/wall3.png", "true" },
                { "5", "/Resource/tiles/wall4.png", "true" },
                { "6", "/Resource/tiles/wall5.png", "true" },
                { "7", "/Resource/tiles/wall6.png", "true" },
                { "8", "/Resource/tiles/key.png", "false" },
                { "9", "/Resource/tiles/chest.png", "false" },
                { "10", "/Resource/tiles/water1.png", "true" },
                { "11", "/Resource/tiles/water2.png", "true" },
                { "12", "/Resource/tiles/water3.png", "true" },
                { "13", "/Resource/tiles/water4.png", "true" },
                { "14", "/Resource/tiles/water5.png", "true" },
                { "20", "/Resource/tiles/bridge1.png", "false" },
                { "21", "/Resource/tiles/bridge2.png", "false" }
        };

        for (String[] t : tiles) {
            int index = Integer.parseInt(t[0]);
            setup(index, t[1], Boolean.parseBoolean(t[2]));
        }
    }

    private void setup(int index, String imagePath, boolean collision) {
        while (tile.size() <= index)
            tile.add(new Tile());
        try (InputStream is = getClass().getResourceAsStream(imagePath)) {
            if (is == null) {
                System.err.println("⚠️ Missing tile: " + imagePath);
                return;
            }
            Tile t = new Tile();
            t.image = ImageIO.read(is);
            t.collision = collision;
            tile.set(index, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clear old data + Reload map
    public void resetAndLoadMap(String filePath) {
        clearMapData();
        loadMap(filePath);
    }

    private void clearMapData() {
        boxPositions.clear();
        playableCharacterPositions.clear();
        enemyPositions.clear();
        for (int c = 0; c < gp.maxScreenCol; c++) {
            for (int r = 0; r < gp.maxScreenRow; r++) {
                mapTileNum[c][r] = 0;
                mapCodes[c][r] = null;
            }
        }
    }

    /** Read tile and object placement from map file. */
    public void loadMap(String filePath) {
        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                System.err.println("Map file not found: " + filePath);
                return;
            }

            java.util.Scanner sc = new java.util.Scanner(is);
            int row = 0;
            while (sc.hasNextLine() && row < gp.maxScreenRow) {
                String line = sc.nextLine();
                String[] numbers = line.split(" ");
                for (int col = 0; col < numbers.length && col < gp.maxScreenCol; col++) {
                    int num = Integer.parseInt(numbers[col]);
                    // store raw code string for hazard manager or other systems
                    mapCodes[col][row] = numbers[col];

                    // Character spawn (90–92)
                    if (num >= 90 && num <= 92) {
                        playableCharacterPositions.add(new int[] { col, row, num - 90 });
                        mapTileNum[col][row] = 0;
                    }
                    // Box spawn
                    else if (num >= 50 && num <= 59) { // 50-59 reserved for boxes
                        int typeIndex = num - 50;
                        boxPositions.add(new int[] { col, row, typeIndex });
                        mapTileNum[col][row] = 0;
                    }
                    // Enemy Spawn
                    else if (num == 80) {
                        // 80 → Bloodshot Eye (Patrolling enemy)
                        enemyPositions.add(new int[] { col, row, 0 }); // 0 = BloodshotEye
                        mapTileNum[col][row] = 0;
                    } else if (num == 81) {
                        // 81 → Ocular Watcher (Stationary enemy)
                        enemyPositions.add(new int[] { col, row, 1 }); // 1 = OcularWatcher
                        mapTileNum[col][row] = 0;
                    }
                    // Button (60-69)
                    else if (num >= 60 && num <= 69) {
                        mapTileNum[col][row] = 0;
                    }
                    // Spike(70-79)
                    else if (num >= 70 & num <= 79) {
                        mapTileNum[col][row] = 0;
                    }
                    // Key and Chest tiles (8, 9) → spawn marker only
                    else if (num == 8 || num == 9) {
                        // Keep as normal code for EntityManager to read later
                        mapTileNum[col][row] = 0; // treat tile as grass
                    }
                    // Mark as itself
                    else {
                        mapTileNum[col][row] = num;
                    }
                }
                row++;
            }

            System.out.println("Characters: " + playableCharacterPositions.size() +
                    " Boxes: " + boxPositions.size() +
                    " Enemies: " + enemyPositions.size());

            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTileCollidable(int col, int row) {
        if (col < 0 || col >= gp.maxScreenCol || row < 0 || row >= gp.maxScreenRow)
            return true;
        int tileNum = mapTileNum[col][row];
        if (tileNum < 0 || tileNum >= tile.size())
            return true;
        Tile t = tile.get(tileNum);
        return t != null && t.collision;
    }

    private void validateMap() {
        for (int c = 0; c < gp.maxScreenCol; c++) {
            for (int r = 0; r < gp.maxScreenRow; r++) {
                int idx = mapTileNum[c][r];
                if (idx < 0 || idx >= tile.size()) {
                    System.err.println(
                            "TileManager.validateMap: invalid tile index " + idx + " at (" + c + "," + r + ")");
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        for (int col = 0; col < gp.maxScreenCol; col++) {
            for (int row = 0; row < gp.maxScreenRow; row++) {
                int tileNum = mapTileNum[col][row];
                if (tileNum < tile.size() && tile.get(tileNum) != null && tile.get(tileNum).image != null) {
                    g2.drawImage(tile.get(tileNum).image, col * gp.tileSize, row * gp.tileSize, gp.tileSize,
                            gp.tileSize, null);
                } else {
                    g2.setColor(java.awt.Color.MAGENTA);
                    g2.fillRect(col * gp.tileSize, row * gp.tileSize, gp.tileSize, gp.tileSize);
                }
            }
        }
    }
}
