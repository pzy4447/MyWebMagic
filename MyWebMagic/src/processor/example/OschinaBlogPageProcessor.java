package processor.example;

import java.util.List;

import basic.Page;
import basic.Site;
import basic.Spider;
import processor.PageProcessor;
import selector.Html;
import selector.Selectable;

/**
 * @author code4crafter@gmail.com <br>
 */
public class OschinaBlogPageProcessor implements PageProcessor {

	private Site site = Site.me().setDomain("cnblogs.com");// my.oschina.net

	@Override
	public void process(Page page) {
		Html html = page.getHtml();
		Selectable s = html.links();
		List<String> links = s.regex(
				"http://my\\.oschina\\.net/flashsword/blog/\\d+").all();
		page.addTargetRequests(links);
		String titleXPathString1 = "//div[@	'BlogEntity']/div[@class='BlogTitle']/h1/text()";
		// 匹配文章标题
		String titleXPathString2 = "//head/title/text()";

		String title = html.xpath(titleXPathString2).toString();
		System.out.printf("title is %s%n", title);
		page.putField("title", title);

		if (page.getResultItems().get("title") == null) {
			// skip this page
			page.setSkip(true);
		}
		page.putField("content", page.getHtml().smartContent().toString());
		page.putField("tags",
				page.getHtml().xpath("//div[@class='BlogTags']/a/text()").all());
		// System.out.printf("%s%n", page.toString());
	}

	@Override
	public Site getSite() {
		return site;

	}

	public static void main(String[] args) {
		String url2 = "http://www.cnblogs.com/yirlin/archive/2006/04/12/373222.html";
		String url1 = "http://www.cnblogs.com/yirlin/archive/2006/04/12/373222.html";

		Spider.create(new OschinaBlogPageProcessor()).addUrl(url2).run();
	}
}
