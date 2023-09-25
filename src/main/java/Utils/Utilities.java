package Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

import static Utils.LCRConstants.SERVICE_URL;
import static Utils.LCRConstants.GET_USERNAME_QUERY;

@Slf4j
public class Utilities {

    public static String formatSlug(String title) {
        title = title.replaceAll("[^a-zA-Z\\d ]", "");
        title = title.replace(' ', '-');
        title = title.toLowerCase();
        return title;
    }

    public static String getUsername(String csrftoken, String leetcodeSession) {
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = callGraphQLService(SERVICE_URL, GET_USERNAME_QUERY, csrftoken, leetcodeSession);
            log.info(String.valueOf(httpResponse.getStatusLine().getStatusCode()));
            String actualResponse = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            JSONObject json = new JSONObject(actualResponse);
            Map obMap = json.toMap();
            obMap = (Map) obMap.get("data");
            obMap = (Map) obMap.get("userStatus");
            return String.valueOf(obMap.get("username"));


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }
        return "";
    }

    public static UserCredentials getUserCredentials() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your csrftoken --> ");
        String csrftoken = sc.next();
        System.out.println();
        System.out.print("Enter your Leetcode session ID --> ");
        String leetcodeSession = sc.next();
        System.out.println();
        String username = getUsername(csrftoken, leetcodeSession);
        username = "\u001B[36m" + username + "\u001B[0m";
        System.out.print("Is " + username + " the correct username [y/n]? ");
        String correctUsername = sc.next().toLowerCase();
        System.out.println();
        if (correctUsername.equals("n")) {
            System.out.print("Would you like to retry [y/n]? ");
            if (sc.next().equals("y")) {
                System.out.println();
                getUserCredentials();
            } else {
                System.out.println("Okay, bye!");
                System.exit(1);
            }
        }
        sc.close();
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setCsrfToken(csrftoken);
        userCredentials.setLeetcodeSession(leetcodeSession);
        return userCredentials;
    }

    public static CloseableHttpResponse callGraphQLService(String url, String query, String csrftoken, String leetcodeSession)
            throws URISyntaxException, IOException {
        BasicCookieStore cookieStore = new BasicCookieStore();
        if (leetcodeSession == null || csrftoken == null) {
            log.error("Missing cookie information");
            if (leetcodeSession == null ^ csrftoken == null) {
                log.error("Leetcode session and csrftoken missing.");
            } else if (leetcodeSession == null) {
                log.error("Leetcode session is missing.");
            } else {
                log.error("csrftoken is missing.");
            }
        }
        BasicClientCookie cookie = new BasicClientCookie("LEETCODE_SESSION", leetcodeSession);
        BasicClientCookie tokenCookie = new BasicClientCookie("csrftoken", csrftoken);
        cookie.setDomain(".leetcode.com");
        tokenCookie.setDomain(".leetcode.com");
        cookie.setPath("/");
        tokenCookie.setPath("/");
        cookieStore.addCookie(cookie);
        cookieStore.addCookie(tokenCookie);
        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build()).setDefaultCookieStore(cookieStore).build();
        HttpGet request = new HttpGet(url);
        URI uri = new URIBuilder(request.getURI())
                .addParameter("query", query)
                .build();
        request.setURI(uri);
        return client.execute(request);
    }
}
