package zhihu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processor.PageProcessor;
import selector.Html;
import selector.Selectable;
import basic.Page;
import basic.Site;
import basic.Spider;

/**
 * @author lenovo 处理问题页面，提取问题的标题、描述以及每个答案的url
 */
public class QuestionPageProcessor implements PageProcessor {
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(0);
	private Map<String, String> articleMap = new HashMap<String, String>();
	private Page currentPage;

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		processByXPath(page);
	}

	public void processByXPath(Page page) {
		Html html = page.getHtml();
		// System.out.printf("html is %s%n", html);
		Selectable s = html.links();
		// 匹配标题
		String titleXPathString = "//head/title/text()";
		String title = html.xpath(titleXPathString).toString();
		// System.out.printf("title is %s%n", title);
		// 问题详情
		String questionDetailXPathExp = "//div[@id='zh-question-detail']/div[@class='zh-summary summary clearfix']/text()";
		String questionDetail = html.xpath(questionDetailXPathExp).get();
		// System.out.printf("questionDesc is : %s%n", questionDetail);
		// 抽取所有答案的url、回答者姓名、回答者ID、答案点赞数
		// 抽取所有回答者姓名
		String answerXPathExp = "//div[@class='zm-item-answer  zm-item-expanded']";
		List<String> answerInfoList = html.xpath(answerXPathExp).all();
		// System.out.printf("answerinfo[0] is : %s%n", answerInfoList.get(0));

		extractAnswerInfo(answerInfoList);

		// 保存到文件
		// saveToPipeline(page);
	}

	public static void main(String[] args) {
		String url1 = "https://www.zhihu.com/question/21324385";
		// String url1 = "http://192.168.1.104:8080/question.html";
		Spider.create(new QuestionPageProcessor()).addUrl(url1).run();
	}

	public void extractAnswerInfo(List<String> answerInfoList) {
		for (String info : answerInfoList) {
			Page page = new Page();
			Html html = new Html(info);
			page.setHtml(html);
			// 回答者姓名
			String authorName = html.xpath("a[@class='author-link']/text()")
					.get();
			if (authorName == null)
				authorName = "匿名用户";
			System.out.printf("authorName is : %s%n", authorName);
			// 回答者url
			String authorUrl = html.regex("author-link.+?href=\"(.+?)\">")
					.get();// html.xpath("a[@class='author-link']/text()").get();
			System.out.printf("authorUrl is : %s%n", authorUrl);
			// 赞数
			String agreeNumString = html.regex("voters text.+>(.+?)人赞同.+?")
					.get();
			System.out.printf("agreeNumString is : %s%n", agreeNumString);
			// 答案url
			String answerUrl = html.regex("data-entry-url=\"(.+?)\">").get();
			System.out.printf("answerUrl is : %s%n", answerUrl);
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
}