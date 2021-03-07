package com.atlassian.test.myPlugin.plugin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.velocity.VelocityManager;
import com.google.common.collect.Maps;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@Component
public class MyServlet extends HttpServlet {



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Map<String , Object> context = Maps.newHashMap();

        Map<String , Integer> map = sortByValue(getMostPopularWordsInIssues(Objects.requireNonNull(getIssues())));
        context.put("map",map);
        VelocityManager velocityManager = ComponentAccessor.getVelocityManager();
        String content = velocityManager.getEncodedBody("/templates", "/words.vm","UTF-8", context);

        resp.getWriter().write(content);
        resp.getWriter().close();

    }


    private List<Issue> getIssues() {
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        ApplicationUser user = authenticationContext.getLoggedInUser();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        Query query = jqlClauseBuilder.project("test").buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();

        SearchResults searchResults = null;
        try {
           searchResults =  ComponentAccessor.getComponentOfType(SearchService.class)
                    .search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        return searchResults != null ? searchResults.getIssues() : null;
    }

    private Map<String ,Integer> getMostPopularWordsInIssues(List<Issue> list){
        Map<String , Integer> map = new HashMap<>();

        for(Issue issue : list){
            String[] description = issue.getDescription().split("\\s+");
            String[] summary = issue.getSummary().split("\\s+");
            String[] both = (String[]) ArrayUtils.addAll(description,summary);
            for(String word : both){
                word = formatString(word);
                if(map.containsKey(word)){
                    map.put(word,map.get(word)+1);
                }else
                    map.put(word,1);
            }
        }

        return map;
    }

    private static  <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());


        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private String formatString(String str){
        return str.toLowerCase().replaceAll("[.,()?!-]+", "");
    }
}
