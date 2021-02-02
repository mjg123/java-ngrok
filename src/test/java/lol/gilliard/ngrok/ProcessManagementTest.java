package lol.gilliard.ngrok;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class ProcessManagementTest {

    @Test
    public void failedStartupWrongBinary(){
        assertThrows(NgrokException.class, () ->
            Ngrok.startClient("date"));
    }

    @Test
    public void failedStartupMissingBinary(){
        assertThrows(NgrokException.class, () ->
            Ngrok.startClient("this does not exist"));
    }

    @Test
    public void failedStartupDoesNotGiveWebConsoleURL(){
        assertThrows(NgrokException.class, () ->
            Ngrok.startClient("this does not exist"));
    }

    @Test
    public void ngrokVersionSuccess(){
        assertTrue(Ngrok.getVersion().length() > 0);
    }

    @Test
    public void ngrokDoesntSupplyCorrectOutput(){
        // this will happen if it can't get to ngrok servers, for example
        assertThrows(NgrokException.class, () ->
            Ngrok.startClient("yes"));
    }

}
