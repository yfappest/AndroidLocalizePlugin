package translate.trans.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import module.Result;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import translate.lang.LANG;
import translate.trans.AbstractTranslator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class I18NTranslator extends AbstractTranslator {

    private static final String URL = "https://i18ns.com/search/i18ns/_search";

    private static final String REPLACE = "@#word#@";
    private static final String QUERY_TEMP = "{\"query\":{\"simple_query_string\":{\"query\":\"" + REPLACE + "\",,\"fields\":[\"translations.zh\"],\"default_operator\":\"AND\",\"analyzer\":\"smartcn\"}},\"size\":12,\"sort\":[{\"_score\":\"desc\"},{\"_score\":\"desc\"},{\"confidence\":\"desc\"}],\"highlight\":{\"fields\":{\"translations.zh\":{}}},\"_source\":[\"name\",\"confidence\",\"domain\",\"format\",\"platform\",\"counts\",\"translations.*\"]}";
    private String query;
    private String target;
    private LANG to;
    private Map<String, String> caches;

    public I18NTranslator() {
        super(URL);
    }

    @Override
    public void setLangSupport() {
        langData.isEmpty();
    }

    @Override
    public void setFormData(LANG from, LANG to, String text) {
        this.to = to;
        target = text;
        query = QUERY_TEMP.replace(REPLACE, target);
    }

    @Override
    public String query() throws Exception {

        if (isExists()) {
            return caches.get(to.getCode());
        }
        URIBuilder uri = new URIBuilder(url);
        HttpPost request = new HttpPost(uri.toString());
        request.setEntity(new StringEntity(query, ContentType.create("application/json", "UTF-8")));
        request.addHeader("authorization", "Basic aTE4bnM6KioqKioq");
        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "utf-8");
        EntityUtils.consume(entity);
        response.getEntity().getContent().close();
        response.close();
        return result;
    }

    private boolean isExists() {
        return caches != null && caches.containsKey(to.getCode());
    }

    @Override
    public String parses(String text) throws IOException {
        if (isExists()) {
            return caches.get(to.getCode());
        }
        caches = new HashMap<>();
        Gson gson = new GsonBuilder().create();
        Result result = gson.fromJson(text, Result.class);
        Result.HitsBeanX hitsBeanX = result.getHits();
        List<Result.HitsBeanX.HitsBean> hits = hitsBeanX.getHits();
        for (Result.HitsBeanX.HitsBean hit : hits) {
            Result.HitsBeanX.HitsBean.Highlight highlight = hit.getHighlight();
            List<String> highlightList = highlight.getTranslations_zh();
            if(highlightList != null && !highlightList.isEmpty()){
                String s = highlightList.get(0);
                String highlightSimple = s.replaceAll("<em>", "").replaceAll("</em>", "<em>");
                if(!target.equals(highlightSimple)){
                    continue;
                }
            }
            Result.HitsBeanX.HitsBean.SourceBean source = hit.get_source();
            List<Map<String, Object>> translations = source.getTranslations();
            for (Map<String, Object> translation : translations) {
                if (translation.containsKey("lang")) {
                    String lang = translation.get("lang").toString();
                    Object o = translation.get(lang);
                    if (o instanceof List) {
                        List<String> list = (List<String>) o;
                        caches.put(lang, list.get(0));
                    }
                }
            }
        }
        return caches.getOrDefault(to.getCode(), "// FIXME: not found target string!");
    }
}
