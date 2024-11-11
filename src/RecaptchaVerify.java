import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class RecaptchaVerify {
    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";


    public static void verify(String gRecaptchaResponse) throws Exception{
        if (gRecaptchaResponse == null || gRecaptchaResponse.isEmpty()) {
            throw new Exception("reCAPTCHA response is missing.");
        }

        URL verifyUrl = new URL(SITE_VERIFY_URL);

        HttpsURLConnection connection = (HttpsURLConnection) verifyUrl.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setDoOutput(true);

        String postParams = "secret=" + RecaptchaConstants.SECRET_KEY + "&response=" + gRecaptchaResponse;

        try (OutputStream outStream = connection.getOutputStream()) {
            outStream.write(postParams.getBytes());
            outStream.flush();
        }

        try (InputStream inputStream = connection.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();

            if (jsonResponse.get("success").getAsBoolean()) {
                return;
            }

            String errorCodes = jsonResponse.has("error-codes") ? jsonResponse.get("error-codes").toString() : "unknown error";
            throw new Exception("reCAPTCHA verification failed: " + errorCodes);
        }
    }
}