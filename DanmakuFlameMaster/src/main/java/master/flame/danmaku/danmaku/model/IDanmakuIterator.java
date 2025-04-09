package master.flame.danmaku.danmaku.model;

public interface IDanmakuIterator {

    BaseDanmaku next();
    
    boolean hasNext();
    
    void reset();

    void remove();
    
}
