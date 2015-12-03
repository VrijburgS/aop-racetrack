import jade.core.AID;

/**
 * Created by dozer on 28-11-2015.
 */
public class Competitor {
    private AID id;
    int[] coordinates = new int[2];
    public Competitor(AID id){
        this.id = id;
        this.coordinates[0] = 0;
        this.coordinates[1] = 0;
    }
    public AID getID(){
        return this.id;
    }
}
