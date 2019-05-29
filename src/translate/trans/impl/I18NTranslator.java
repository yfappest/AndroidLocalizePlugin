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
    //    private static final String QUERY_TEMP = "{\"query\":{\"simple_query_string\":{\"query\":\"" + REPLACE + "\",\"fields\":[\"translations.zh\"],\"default_operator\":\"AND\",\"analyzer\":\"smartcn\"}},\"size\":12,\"sort\":[{\"_score\":\"desc\"},{\"_score\":\"desc\"},{\"confidence\":\"desc\"}],\"highlight\":{\"fields\":{\"translations.zh\":{}}},\"_source\":[\"name\",\"confidence\",\"domain\",\"format\",\"platform\",\"counts\",\"translations.*\"]}";
    private static final String QUERY_TEMP = "{\"query\":{\"simple_query_string\":{\"query\":\"" + REPLACE + "\",\"fields\":[\"translations.en\"],\"default_operator\":\"AND\",\"analyzer\":\"standard\"}},\"size\":12,\"sort\":[{\"_score\":\"desc\"},{\"confidence\":\"desc\"}],\"highlight\":{\"fields\":{\"translations.en\":{}}},\"suggest\":{\"text\":\"China\",\"suggestions\":{\"phrase\":{\"field\":\"translations.en\",\"real_word_error_likelihood\":0.95,\"max_errors\":1,\"gram_size\":4,\"direct_generator\":[{\"field\":\"translations.en\",\"suggest_mode\":\"always\",\"min_word_length\":1}]}}},\"_source\":[\"name\",\"confidence\",\"domain\",\"format\",\"platform\",\"counts\",\"translations.*\"]}";
    private String query;
    private String target;
    private LANG to;
    private Map<String, Map<String, String>> caches;

    public I18NTranslator() {
        super(URL);
        caches = new HashMap<>();
    }

    @Override
    public void setLangSupport() {
        langData.add(LANG.Albanian);
        langData.add(LANG.Arabic);
        langData.add(LANG.Amharic);
        langData.add(LANG.Azerbaijani);
        langData.add(LANG.Irish);
        langData.add(LANG.Estonian);
        langData.add(LANG.Basque);
        langData.add(LANG.Belarusian);
        langData.add(LANG.Bulgarian);
        langData.add(LANG.Icelandic);
        langData.add(LANG.Polish);
        langData.add(LANG.Bosnian);
        langData.add(LANG.Persian);
        langData.add(LANG.Afrikaans);
        langData.add(LANG.Danish);
        langData.add(LANG.German);
        langData.add(LANG.Russian);
        langData.add(LANG.French);
        langData.add(LANG.Filipino);
        langData.add(LANG.Finnish);
        langData.add(LANG.Frisian);
        langData.add(LANG.Khmer);
        langData.add(LANG.Georgian);
        langData.add(LANG.Gujarati);
        langData.add(LANG.Kazakh);
        langData.add(LANG.HaitianCreole);
        langData.add(LANG.Korean);
        langData.add(LANG.Hausa);
        langData.add(LANG.Dutch);
        langData.add(LANG.Kyrgyz);
        langData.add(LANG.Galician);
        langData.add(LANG.Catalan);
        langData.add(LANG.Czech);
        langData.add(LANG.Kannada);
        langData.add(LANG.Corsican);
        langData.add(LANG.Croatian);
        langData.add(LANG.Kurdish);
        langData.add(LANG.Latin);
        langData.add(LANG.Latvian);
        langData.add(LANG.Laotian);
        langData.add(LANG.Lithuanian);
        langData.add(LANG.Luxembourgish);
        langData.add(LANG.Romanian);
        langData.add(LANG.Malagasy);
        langData.add(LANG.Maltese);
        langData.add(LANG.Marathi);
        langData.add(LANG.Malayalam);
        langData.add(LANG.Malay);
        langData.add(LANG.Macedonian);
        langData.add(LANG.Maori);
        langData.add(LANG.Mongolian);
        langData.add(LANG.Bengali);
        langData.add(LANG.Burmese);
        langData.add(LANG.Hmong);
        langData.add(LANG.Xhosa);
        langData.add(LANG.Zulu);
        langData.add(LANG.Nepali);
        langData.add(LANG.Norwegian);
        langData.add(LANG.Punjabi);
        langData.add(LANG.Portuguese);
        langData.add(LANG.Pashto);
        langData.add(LANG.Chichewa);
        langData.add(LANG.Japanese);
        langData.add(LANG.Swedish);
        langData.add(LANG.Samoan);
        langData.add(LANG.Serbian);
        langData.add(LANG.Sotho);
        langData.add(LANG.Sinhala);
        langData.add(LANG.Esperanto);
        langData.add(LANG.Slovak);
        langData.add(LANG.Slovenian);
        langData.add(LANG.SwahiliSwahili);
        langData.add(LANG.ScottishGaelic);
        langData.add(LANG.Cebuano);
        langData.add(LANG.Somali);
        langData.add(LANG.Tajik);
        langData.add(LANG.Telugu);
        langData.add(LANG.Tamil);
        langData.add(LANG.Thai);
        langData.add(LANG.Turkish);
        langData.add(LANG.Welsh);
        langData.add(LANG.Urdu);
        langData.add(LANG.Ukrainian);
        langData.add(LANG.Uzbek);
        langData.add(LANG.Spanish);
        langData.add(LANG.Hebrew);
        langData.add(LANG.Greek);
        langData.add(LANG.Hawaiian);
        langData.add(LANG.Sindhi);
        langData.add(LANG.Hungarian);
        langData.add(LANG.Shona);
        langData.add(LANG.Armenian);
        langData.add(LANG.Igbo);
        langData.add(LANG.Italian);
        langData.add(LANG.Yiddish);
        langData.add(LANG.Hindi);
        langData.add(LANG.Sundanese);
        langData.add(LANG.Indonesian);
        langData.add(LANG.Javanese);
        langData.add(LANG.English);
        langData.add(LANG.Yoruba);
        langData.add(LANG.Vietnamese);
        langData.add(LANG.ChineseTraditional);
        langData.add(LANG.ChineseSimplified);
    }

    @Override
    public void setFormData(LANG from, LANG to, String text) {
        this.to = to;
        target = text;
        query = QUERY_TEMP.replace(REPLACE, target);
        caches.put(text, new HashMap<>());
    }

    @Override
    public String query() throws Exception {
        Map<String, String> cache = caches.get(target);
        if (isExists(cache)) {
            String key = to.getCode().replace("-CN", "").replace("-", "_");
            return cache.getOrDefault(key, null);
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

    private boolean isExists(Map<String, String> cache) {
        String key = to.getCode().replace("-CN", "").replace("-", "_");
        return cache != null && cache.containsKey(key);
    }

    @Override
    public String parses(String text) throws IOException {
        Map<String, String> cache = caches.get(target);
        if (isExists(cache)) {
            return text;
        }
        Gson gson = new GsonBuilder().create();
        Result result = gson.fromJson(text, Result.class);
        Result.HitsBeanX hitsBeanX = result.getHits();
        List<Result.HitsBeanX.HitsBean> hits = hitsBeanX.getHits();
        for (Result.HitsBeanX.HitsBean hit : hits) {
            Result.HitsBeanX.HitsBean.Highlight highlight = hit.getHighlight();
            List<String> highlightList = highlight.getTranslations_en();
            if (highlightList != null && !highlightList.isEmpty()) {
                String s = highlightList.get(0);
                String highlightSimple = s.replaceAll("<em>", "").replaceAll("</em>", "");
                if (!target.equals(highlightSimple)) {
                    continue;
                }
            }
            Result.HitsBeanX.HitsBean.SourceBean source = hit.get_source();
            List<Map<String, Object>> translations = source.getTranslations();
            for (Map<String, Object> translation : translations) {
                if (translation.containsKey("lang")) {
                    String lang = translation.get("lang").toString();
                    if (cache.containsKey(lang)) {
                        continue;
                    }
                    Object o = translation.get(lang);
                    if (o instanceof List) {
                        List<String> list = (List<String>) o;
                        cache.put(lang, list.get(0));
                    }
                }
            }
        }
        String key = to.getCode().replace("-CN", "").replace("-", "_");
        return cache.getOrDefault(key, null);
    }
}
