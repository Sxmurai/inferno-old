package cope.inferno.core.gui.font;

public class CharData {
    private final char c;
    private final int x, y, width, height;

    public CharData(char character, int x, int y, int width, int height) {
        this.c = character;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public char getCharacter() {
        return c;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
