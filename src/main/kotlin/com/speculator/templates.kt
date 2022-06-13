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
                            .section-content {
                                display: flex;
                                flex-direction: row;
                            }
                            .section-content > div {
                                margin-right: 16px;
                            }
                            .add-station {
                                margin-top: 16px;
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
