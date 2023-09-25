package Utils;

public class LCRConstants {

    public static final String PROGRAM_STARTED = "Command line program started";
    public static final String DESKTOP_SUPPORTED = "Desktop is supported: ";
    public static final String SERVICE_URL = "https://leetcode.com/graphql";
    public static final String GET_USERNAME_QUERY = "{userStatus{username}}";
    public static final String GET_QUESTIONS_QUERY = "{questionList (categorySlug: \"algorithms\" limit: 5000 " +
            "filters: {}) {data{title status}}}";
    public static final String RANDOM_QUESTION_URL_PREFIX = "https://leetcode.com/problems/";
}
