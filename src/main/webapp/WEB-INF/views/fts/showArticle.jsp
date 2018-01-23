<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body lang="zh-Hans">
<div class="container one-collumn sidebar-position-left page-post-detail">
    <div class="headband"></div>
    <%@ include file="header.jsp" %>
    <main id="main" class="main">
        <div class="main-inner">
            <div class="content-wrap">
                <div id="content" class="content">
                    <div id="posts" class="posts-expand">
                        <article class="post post-type-normal">
                            <header class="post-header">
                                <h1 class="post-title">
                                    我在阿里的这两年
                                </h1>

                                <div class="post-meta">
                                    <span class="post-time">
                                        <span class="post-meta-item-icon">
                                            <i class="fa fa-calendar-o"></i>
                                        </span>
                                        <span class="post-meta-item-text">发表于</span>
                                        <time  datetime="2017-07-16T20:43:38+08:00" content="2017-07-16">
                                        </time>
                                    </span>

                                    <span class="post-category">
                                      &nbsp; | &nbsp;
                                      <span class="post-meta-item-icon">
                                        <i class="fa fa-folder-o"></i>
                                      </span>
                                        <span class="post-meta-item-text">分类于</span>
                                        <span itemprop="about" itemscope itemtype="https://schema.org/Thing">
                                            <a href="../../../../../categories/%e8%81%8c%e5%9c%ba%e7%94%9f%e6%b6%af/index.html" itemprop="url" rel="index">
                                                <span itemprop="name">职场生涯</span>
                                            </a>
                                        </span>

                                    </span>

                                    <span class="post-comments-count">
                                        &nbsp; | &nbsp;
                                        <a href="index.html#comments" itemprop="discussionUrl">
                                          <span class="post-comments-count disqus-comment-count" data-disqus-identifier="blog/2017/07/16/two-years-in-alibaba/" itemprop="commentsCount"></span>
                                        </a>
                                    </span>

                                    <span id="/blog/2017/07/16/two-years-in-alibaba/" class="leancloud_visitors" data-flag-title="我在阿里的这两年">
                                       &nbsp; | &nbsp;
                                       <span class="post-meta-item-icon">
                                         <i class="fa fa-eye"></i>
                                       </span>
                                        <span class="post-meta-item-text">阅读次数 </span>
                                        <span class="leancloud-visitors-count"></span>
                                    </span>
                                </div>
                            </header>

                            <div class="post-body" itemprop="articleBody">
                                <p>这两天，团队来了不少新人，有实习生也有刚毕业的大学生。看着他们稚气的面庞，文静而腼腆的样子，跟我两年前刚进公司的时候好像。今天内网提示我这是我在阿里的第732天，两周年快乐。突然觉得，我应该写点什么东西，一方面给自己一个总结，分享一下自己成长的故事。另一方面，希望能帮助刚毕业的大学生们消除一些成长路上的疑惑。</p><p>
                                <img src="../../../../../../ww1.sinaimg.cn/large/81b78497gy1fhljtdhofaj21ue0s6x6q.jpg" alt=""></p>
                                <a id="more"></a>
                                <h2 id="JStorm"><a href="#JStorm" class="headerlink" title="JStorm"></a>JStorm</h2>
                                <p>我是2015年7月毕业后加入的公司，当时进入的是中间件-实时计算 JStorm 团队。JStorm 是用 Java 语言代替 Clojure 语言重写了 Apache Storm，并在原有的基础上做了诸多性能和功能优化。JStorm 是阿里巴巴开源的几个明星产品之一，在国内的用户非常多。很多国内做流计算，实时计算的应该都知道 JStorm。</p>
                                <p>但是我当时并不知道 JStorm 或是 Storm，我当时只知道 Spark，也不懂什么是实时计算，流计算。所以<br>都是入职之后现学，看文档，看源码，学习 Clojure。差不多11月份的时候，阿里正式向 Apache 基金会捐赠了 JStorm。这是阿里捐赠给 Apache 的第一个项目，后面还有
                                    <a href="https://rocketmq.incubator.apache.org/" target="_blank" rel="external">Apache RocketMQ</a>,
                                    <a href="https://weex.incubator.apache.org/cn/" target="_blank" rel="external">Apache Weex</a>。JStorm 火了一把，当时有很多报道转载这件事情，还放了一张我们团队油头垢面、屌丝气十足的合照。</p>
                                <p>在第一个半年里，我重写了 JStorm 的开源 UI，参与了管控平台的开发，做了 JStorm 的一些开发工作。到了半年 Review 的时候，我觉得我做的并不好（虽然老板一直鼓励我做的挺好…）。其实作为一个新人，半年时间是很难做出成绩的。但是从一开始我对自己的期望就比较高，所以失望也比较大。而且看到同期的应届生半年就有非常出色的成果，相比之下就相形见绌了。那段时间，自己的心情也比较低落，比较迷茫。我觉得我脑子不笨，学习能力也不差，工作也很努力，为什么就做不出什么“成果”呢？</p>
                                <p>在阿里中间件，有很多非常耀眼的新人，有的在我还在熟悉工作的时候人家已经成为了项目 owner，有的已经成为了 Apache 项目的 PMC，有的一年就晋升到了 P6。他们有很多地方是值得学习的，聪明，拼搏，机遇，是我觉得最重要的几个关键词。但是工作后你就会发现，哪有那么多的机会在等你呢。不过，关于个人成长和晋升，阿里有句老话叫做“没有坑，就让自己先成为萝卜”，我非常认同，是萝卜的话，那个坑是迟早的事情。</p>
                                <h2 id="Flink"><a href="#Flink" class="headerlink" title="Flink"></a>Flink</h2>
                                <p>2015 年是流计算百花齐放的时代，各个流计算框架层出不穷。Storm, JStorm, Heron, Flink, Spark Streaming, Google Dataflow (后来的 Beam) 等等。其中 Flink 的一致性语义和最接近 Dataflow 模型的开源实现，使其成为流计算框架中最耀眼的一颗。我也是看中了 Flink 在流计算上的先进性，所以在 2016 年春节过后回来上班的第一天，我跟老板提议，希望能去研究 Flink，我们团队需要有个人透彻了解 Flink 的原理（我希望成为 Flink 的萝卜，事实证明这为我之后带来了很多机会）。Boss 同意了，并且给了我一个 KPI：一年内成为 Flink Committer。</p>
                                <p>后来我才知道搜索部门已经研究 Flink 有一年了，并且有了个内部版本，名叫“Blink”。所以之后，我们便与 Blink 开始共建 Table API &amp; SQL。我也是在那个时候进入 Flink 社区工作。那个时候在社区工作是非常孤独，非常艰难的，因为很多时候没有人可以一起讨论，一个人在社区推进事情也很困难。不过，当你推进的提议被社区所接受并落地的那种心情是非常有成就感的。</p>
                                <p>记得刚进入社区工作的时候，我也只能挑一些非常简单的修复 Bug 的任务，学习社区的工作流程，用我非常蹩脚的英文在 GitHub 上与他们交流。有时候，每写一句话都要粘贴到 Google 翻译中翻译一遍，确保自己语句没有问题。我对学习英语没什么天赋，大学考英语四级考了三次才过。但是孰能生巧，现在我也可以流畅地在 GitHub 、邮件列表上与他们交流，能洋洋洒洒地写几千单词的英文设计文档与社区讨论，能与 dataArtisans 电话会议讨论设计细节。英语是 IT 人士非常重要的软实力，我觉得至少要做到读英文技术文章不吃力，其次要做到能用英语流畅交流技术。我现在已经越来越体会到英语的重要性，我觉得英语在某些方面甚至决定了你的潜力，为此我还买了一个半年的英语课程，每天练习自己的听力和口语，现在已经坚持了一个半月了，希望能坚持到底。</p>
                                <p>在社区工作了将近一年，终于在 2017 年春节的时候，收到了社区邀请我成为 Flink Committer 的邮件，这是对我过去一年工作的肯定，也算是踩点完成了 KPI … 我很荣幸是阿里第一位成为 Flink Committer 的。截止到目前，阿里已经发展了 5 位 Flink Committer，当然都在我们大团队 😉。</p>
                                <h2 id="Blink"><a href="#Blink" class="headerlink" title="Blink"></a>Blink</h2>
                                <p>阿里还有一句老话叫“拥抱变化”。大概五月份的时候，为了打造世界级的流计算引擎和服务，我们团队和 Blink 都加入了新成立的计算平台事业部。在新的事业部，我迎来了自己的第一次晋升。晋升的过程非常愉快。令我印象深刻的是，在这么一个P5到P6的晋升面试上居然要出动1个P10，1个P9，3个P8，1个HRG的阵容。公司也真是舍得下成本啊。</p>
                                <p>虽然“拥抱变化”了，不过在新事业部我做的事情没什么改变，仍然是 Flink/Blink SQL 相关的工作。从一开始我们就意识 SQL 在抽象和统一用户业务逻辑上的强大之处。而且，流和批的计算可以自然而然的在传统SQL这一层统一。SQL 可以把一个非常复杂的计算用简单的抽象表达出来。这是我认为我现在工作有意思有挑战的地方。在社区方面，Flink SQL 总共有 5 位 Committer，我们团队占据了其中三位。我们与社区的合作是非常紧密的，平均每个星期都有与 dataArtisans （Flink 背后的公司） 的视频会议，讨论每周的技术设计细节。可以说，流计算 SQL 在开源范围内我们是比较领先的，甚至是在定义流计算SQL的标准。dataArtisans 的 CTO Stephan 说，阿里巴巴是 Flink 现在的最大的贡献者。确实如此，不仅在 Flink SQL，在 Runtime 方面，我们帮助社区贡献了若干从大规模部署到性能，再到容错方面的优化。这些优化使得 Flink 的易用性和性能得到了大大的提升。</p>
                                <p>哈哈，是的，这里不免落俗地发个招聘广告。阿里实时计算团队（杭州，北京，美国）诚邀各种牛人（存储，计算，分布式，大数据，甚至前端）加入，有感兴趣的可以直接联系我：imjark#gmail.com 。</p>
                            </div>
                            <div>
                                <div class="copyright-txt">
                                    <a href="../../../../../copyright/index.html"><i class="fa fa-copyright"></i>著作权归作者所有</a>
                                </div>
                            </div>

                            <footer class="post-footer">
                                <div class="post-tags">
                                    <a href="../../../../../tags/%e8%81%8c%e5%9c%ba%e7%94%9f%e6%b6%af/index.html" rel="tag">#职场生涯</a>
                                </div>
                                <div class="post-spread">
                                    <div class="jiathis_style">
                                        <span class="jiathis_txt">分享到：</span>
                                        <a class="jiathis_button_tsina">新浪微博</a>
                                        <a class="jiathis_button_weixin">微信</a>
                                        <a class="jiathis_button_twitter">Twitter</a>
                                        <a class="jiathis_button_fb">Facebook</a>
                                        <a href="http://www.jiathis.com/share" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank">更多</a>
                                        <a class="jiathis_counter_style"></a>
                                    </div>
                                    <script type="text/javascript" src="../../../../../../v3.jiathis.com/code/jiac891.js?uid=" charset="utf-8"></script>
                                </div>

                                <div class="post-nav">
                                    <div class="post-nav-next post-nav-item">
                                        <a href="../../../05/17/flink-internals-async-io/index.html" rel="next" title="Flink 原理与实现：Aysnc I/O">
                                            <i class="fa fa-chevron-left"></i> Flink 原理与实现：Aysnc I/O
                                        </a>
                                    </div>

                                    <div class="post-nav-prev post-nav-item"></div>
                                </div>
                            </footer>
                        </article>
                    </div>
                </div>

                <div class="comments" id="comments">
                    <div id="disqus_thread">
                        <noscript>
                            Please enable JavaScript to view the
                            <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a>
                        </noscript>
                    </div>

                </div>

            </div>

            <div class="sidebar-toggle">
                <div class="sidebar-toggle-line-wrap">
                    <span class="sidebar-toggle-line sidebar-toggle-line-first"></span>
                    <span class="sidebar-toggle-line sidebar-toggle-line-middle"></span>
                    <span class="sidebar-toggle-line sidebar-toggle-line-last"></span>
                </div>
            </div>

            <aside id="sidebar" class="sidebar">
                <div class="sidebar-inner">

                    <ul class="sidebar-nav motion-element">
                        <li class="sidebar-nav-toc sidebar-nav-active" data-target="post-toc-wrap">
                            文章目录
                        </li>
                        <li class="sidebar-nav-overview" data-target="site-overview">
                            站点概览
                        </li>
                    </ul>

                    <section class="site-overview sidebar-panel ">
                        <div class="site-author motion-element" itemprop="author" itemscope itemtype="http://schema.org/Person">
                            <img class="site-author-image" itemprop="image" src="../../../../../images/default_avatar.jpg" alt="WuChong" />
                            <p class="site-author-name" itemprop="name">WuChong</p>
                            <p class="site-description motion-element" itemprop="description">当你的才华还撑不起你的野心时，<br/>你就应该静下心来学习。</p>
                        </div>
                        <nav class="site-state motion-element">
                            <div class="site-state-item site-state-posts">
                                <a href="../../../../../archives/index.html">
                                    <span class="site-state-item-count">70</span>
                                    <span class="site-state-item-name">日志</span>
                                </a>
                            </div>

                            <div class="site-state-item site-state-categories">

                                <a href="../../../../../categories/index.html">

                                    <span class="site-state-item-count">11</span>
                                    <span class="site-state-item-name">分类</span>
                                </a>
                            </div>

                            <div class="site-state-item site-state-tags">
                                <a href="../../../../../tags/index.html">
                                    <span class="site-state-item-count">65</span>
                                    <span class="site-state-item-name">标签</span>
                                </a>
                            </div>

                        </nav>

                        <div class="feed-link motion-element">
                            <a href="../../../../../atom.xml" rel="alternate">
                                <i class="fa fa-rss"></i> RSS
                            </a>
                        </div>

                        <div class="links-of-author motion-element">

                        </div>

                        <div class="cc-license motion-element" itemprop="license">
                            <a href="http://creativecommons.org/licenses/by-nc-sa/4.0" class="cc-opacity" target="_blank">
                                <img src="../../../../../images/cc-by-nc-sa.svg" alt="Creative Commons" />
                            </a>
                        </div>

                        <div class="links-of-author motion-element">

                            <p class="site-author-name">Links</p>

                            <span class="links-of-author-item">
                    <a href="http://jm.taobao.org/" target="_blank">阿里中间件博客</a>
                  </span>

                        </div>

                    </section>

                    <section class="post-toc-wrap motion-element sidebar-panel sidebar-panel-active">
                        <div class="post-toc-indicator-top post-toc-indicator">
                            <i class="fa fa-angle-double-up"></i>
                        </div>
                        <div class="post-toc">

                            <div class="post-toc-content">
                                <ol class="nav">
                                    <li class="nav-item nav-level-2">
                                        <a class="nav-link" href="#JStorm"><span class="nav-number">1.</span> <span class="nav-text">JStorm</span></a>
                                    </li>
                                    <li class="nav-item nav-level-2">
                                        <a class="nav-link" href="#Flink"><span class="nav-number">2.</span> <span class="nav-text">Flink</span></a>
                                    </li>
                                    <li class="nav-item nav-level-2">
                                        <a class="nav-link" href="#Blink"><span class="nav-number">3.</span> <span class="nav-text">Blink</span></a>
                                    </li>
                                </ol>
                            </div>

                        </div>
                        <div class="post-toc-indicator-bottom post-toc-indicator">
                            <i class="fa fa-angle-double-down"></i>
                        </div>
                    </section>

                </div>
            </aside>
        </div>
    </main>
</div>


<%@ include file="footer.jsp" %>
</body>
</html>
