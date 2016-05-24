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
 * @since 0.3.2
 */
public class GithubRepoPageProcessor implements PageProcessor {
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(0);

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// 部分二：定义如何抽取页面信息，并保存下来
		Html html = page.getHtml();
		System.out.printf("html is %s%n", html);

		Selectable s = html.links();

		String regex1 = "(https://github\\.com/[\\w\\-]+/[\\w\\-])";// 匹配形如"https://github.com/x\-/x\-"的url
		String regex2 = "(https://github\\.com/[\\w\\-])";// 匹配形如"https://github.com/x\-"的url

		List<String> requests1 = s.regex(regex1).all();
		page.addTargetRequests(requests1);
		List<String> request2 = s.regex(regex2).all();
		page.addTargetRequests(request2);
		System.out.printf("regex1 is %s%n", regex1);
		System.out.printf("regex2 is %s%n", regex2);

		// 匹配？并抽取内容
		page.putField("author",
				page.getUrl().regex("https://github\\.com/(\\w+)/.*")
						.toString());
		// 在html中查找class属性为'entry-title
		// public'的h1元素，并找到他的strong子节点的a子节点，并提取a节点的文本信息
		page.putField(
				"name",
				page.getHtml()
						.xpath("//h1[@class='entry-title public']/strong/a/text()")
						.toString());
		if (page.getResultItems().get("name") == null) {
			// skip this page
			page.setSkip(true);
		}
		// 匹配？并抽取内容
		page.putField("readme",
				page.getHtml().xpath("//div[@id='readme']/tidyText()"));
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		// 从"https://github.com/code4craft"开始抓
		Spider.create(new GithubRepoPageProcessor())
				.addUrl("https://github.com/code4craft").thread(5).run();
	}
}
