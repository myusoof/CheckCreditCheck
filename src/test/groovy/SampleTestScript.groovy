import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Created with IntelliJ IDEA.
 * User: yusoof
 * Date: 24/08/14
 * Time: 12:57
 * To change this template use File | Settings | File Templates.
 */
class SampleTestScript {
    public static void main(String[] args) {
        println DateFormat.getDateInstance().format(new Date())
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMDD")
        println dateFormat.format(new Date())
    }
}
