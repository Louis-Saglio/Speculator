package com.speculator

import io.ktor.server.html.*
import io.ktor.server.html.HtmlContent
import kotlinx.html.*

fun <TTemplate : Template<HTML>> renderTemplate(
    template: TTemplate,
    body: TTemplate.() -> Unit
): HtmlContent {
    template.body()
    return HtmlContent { with(template) { apply() } }
}

class DefaultTemplate : Template<HTML> {
    val pageTitle = Placeholder<FlowContent>()
    val content = Placeholder<FlowContent>()
    val tabTitle = Placeholder<TITLE>()

    override fun HTML.apply() {
        head {
            title {
                insert(tabTitle)
            }
            script(type = ScriptType.textJavaScript) {
                unsafe {
                    //language=JavaScript
                    raw("window.setInterval(() => this.location.reload(), 20_000)")
                }
            }
            style(type = StyleType.textCss) {
                unsafe {
                    raw(
                        //language=CSS
                        """
                            body * {
                                display: flex;
                                flex-direction: column;
                                margin: 0;
                                padding: 0;
                            }
                            h1 {
                                padding: 0 0 16px 0;
                            }
                            h2 {
                                padding: 20px 0 16px 0;
                            }
                            h4 {
                                padding: 10px 0 8px 0;
                            }
                            .section-content {
                                flex-direction: row;
                            }
                            .section-content > div {
                                padding-right: 16px;
                            }
                            .refresh-button {
                                padding-top: 16px;
                            }
                            button {
                                margin-top: 32px;
                                padding: 8px;
                            }
                        """.trimIndent()
                    )
                }
            }
        }
        body {
            h1 {
                insert(this@DefaultTemplate.pageTitle)
            }
            insert(content)
            button {
                //language=JavaScript
                onClick = "location.reload()"
                +"Refresh"
            }
        }
    }
}
