package processor.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import basic.Page;
import basic.Site;
import basic.Spider;
import pipeline.FilePipeline;
import processor.PageProcessor;
import selector.Html;
import selector.Selectable;

public class ZhihuProcessorTest implements PageProcessor {
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(0);
	private Map<String, String> articleMap = new HashMap<String, String>();

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// processByCss(page);
		// processByXPath(page);
		processByReg(page);
	}

	public void processByCss(Page page) {
		Html html = page.getHtml();
		System.out.printf("html is %s%n", html);
		Selectable s = html.links();
		// 匹配本页面标题
		String titleCssExp = "head title";
		String title = html.css(titleCssExp).toString();
		System.out.printf("title is %s%n", title);
		// 抽取相关问题的标题和url
		String articleCssExp = "a[class=question_link]";
		List<String> articleInfoList = html.css(articleCssExp).all();
		System.out.printf("info is : %s%n", articleInfoList.get(0));
		// 抽取文章信息
		extractArticle(articleInfoList);
	}

	public void processByXPath(Page page) {
		Html html = page.getHtml();
		System.out.printf("html is %s%n", html);
		Selectable s = html.links();
		// 匹配文章标题
		String titleXPathString = "//head/title/text()";
		String title = html.xpath(titleXPathString).toString();
		System.out.printf("title is %s%n", title);

		String articleInfoXPathExp = "//h2/a[@class='question_link']";// /text()
		List<String> articleInfoList = html.xpath(articleInfoXPathExp).all();
		System.out.printf("info is : %s%n", articleInfoList.get(0));
		// 抽取文章信息
		extractArticle(articleInfoList);
	}

	public void processByReg(Page page) {
		Html html = page.getHtml();
		System.out.printf("html is %s%n", html);
		Selectable s = html.links();
		// 匹配文章标题
		String title = html.regex("<title>(.+?)</title>").toString();
		System.out.printf("title is %s%n", title);
		//
		String articleInfoRegExp = "<a class=\"question_link\".+?</a>";
		List<String> articleInfoList = html.regex(articleInfoRegExp).all();
		System.out.printf("info is : %s%n", articleInfoList.get(0));

		// 抽取文章信息
		extractArticle(articleInfoList);

		// 保存到文件
		saveToPipeline(page);
	}

	public void extractArticle(List<String> articleInfoList) {
		for (String info : articleInfoList) {
			// title
			String articletitle = getMatchedString(">(.+?)</a>", info);
			if (articletitle == null)
				continue;
			// url
			String articleurl = getMatchedString("href=\"(.+?)\"", info);
			// System.out.printf("find a title : %s%n", articletitle);
			// System.out.printf("find a url : %s%n", articleurl);
			articleMap.put(articletitle, articleurl);
		}
	}

	public void saveToPipeline(Page page) {
		int i = 1;
		for (String title : articleMap.keySet()) {
			page.putField("title" + i, title);
			page.putField("url" + i, articleMap.get(title));
			i++;
		}
	}

	public String getMatchedString(String rex, String content) {
		String matchedString = null;
		Pattern pattern = Pattern.compile(rex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			matchedString = matcher.group(1);
		}
		return matchedString;
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		// 从"https://github.com/code4craft"开始抓
		String url1 = "https://www.zhihu.com/explore/recommendations";
		Spider.create(new ZhihuProcessor()).addUrl(url1)
				.addPipeline(new FilePipeline("C:\\Users\\lenovo\\Desktop\\"))
				.run();

		// ResultItems = spider.
	}
}
