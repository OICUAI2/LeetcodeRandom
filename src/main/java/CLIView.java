
import Utils.UserCredentials;
import Utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static Utils.LCRConstants.*;

@Slf4j
public class CLIView {

    private static final Scanner SC = new Scanner(System.in);
    String csrftoken;
    String leetcodeSession;
    Utilities utils = new Utilities();

    public void run() {
        log.info(PROGRAM_STARTED);
        printBanner();
        System.out.println("Welcome to the LC Randomizer.");
        // TODO ask for cookie info
        UserCredentials userCredentials = Utilities.getUserCredentials();
        csrftoken = userCredentials.getCsrfToken();
        leetcodeSession = userCredentials.getLeetcodeSession();
        try {
            System.out.print("Choosing a random solved problem from your profile");
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.print(".");
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.print(".");
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.print(".");
            String questionTitleSlug = chooseRandomQuestion();
            String url = RANDOM_QUESTION_URL_PREFIX + questionTitleSlug;
            openInBrowser(url);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private String chooseRandomQuestion() {

        ArrayList<String> titles = getSolvedQuestions();
        Random random = new Random();
        int randomIndex = random.nextInt(titles.size());
        return titles.get(randomIndex);

    }

    private ArrayList<String> getSolvedQuestions() {
        CloseableHttpResponse httpResponse = null;
        // TODO maybe change this to Optional
        ArrayList<String> questions = null;
        try {
            httpResponse = utils.callGraphQLService(SERVICE_URL, GET_QUESTIONS_QUERY, csrftoken, leetcodeSession);
            log.info(String.valueOf(httpResponse.getStatusLine().getStatusCode()));
            String actualResponse = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            JSONObject json = new JSONObject(actualResponse);

            Map obMap = json.toMap();
            obMap = (Map) obMap.get("data");
            obMap = (Map) obMap.get("questionList");
            questions = filterSolved(new ArrayList((Collection) obMap.get("data")));

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }
        return questions;
    }

    private ArrayList filterSolved(ArrayList questions) {
        ArrayList<String> filteredQs = new ArrayList();
        for (int i = 0; i < questions.size(); i++) {
            HashMap temp = (HashMap) questions.get(i);
            String val = String.valueOf(temp.get("status"));
            if (val.equals("ac")) {
                String titleSlug = Utilities.formatSlug(String.valueOf(temp.get("title")));
                filteredQs.add(titleSlug);
            }
        }
        return filteredQs;
    }

        private void openInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            log.info(DESKTOP_SUPPORTED + Desktop.isDesktopSupported());
            Desktop desktop = Desktop.getDesktop();
            try {
                URI uri = new URI(url);
                desktop.browse(uri);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            log.error(DESKTOP_SUPPORTED + Desktop.isDesktopSupported());
        }
    }

    private void printBanner() {
        System.out.print("\033[H\033[2J");
        System.out.println("  _     ____   ____                 _                 ");
        System.out.println(" | |   / ___| |  _ \\ __ _ _ __   __| | ___  _ __ ___  ");
        System.out.println(" | |  | |     | |_) / _` | '_ \\ / _` |/ _ \\| '_ ` _ \\ ");
        System.out.println(" | |__| |___  |  _ < (_| | | | | (_| | (_) | | | | | |");
        System.out.println(" |_____\\____| |_| \\_\\__,_|_| |_|\\__,_|\\___/|_| |_| |_|");
        System.out.println("        ");
    }
}
