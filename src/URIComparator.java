import java.net.URI;
import java.util.Comparator;


public class URIComparator implements Comparator<URI> {
    @Override
    public int compare(URI o1, URI o2) {
        return o1.toString().compareTo(o2.toString());
    }
}