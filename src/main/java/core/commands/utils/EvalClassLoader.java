package core.commands.utils;

public class EvalClassLoader extends ClassLoader {
    public void define(byte[] bytes) {
        super.defineClass(null, bytes, 0, bytes.length);
    }
}
