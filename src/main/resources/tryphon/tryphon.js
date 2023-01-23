window.onload = function () {
    let expectedText = "lorem ipsum dolor sit amet"
    let index = 0
    displayPlayGround(expectedText, index)
    document.onkeydown = async (event) => {
        if (event.key === expectedText[index]) {
            index += 1
        }
        if (index === expectedText.length - 1) {
            expectedText = await generateText()
            console.log(expectedText)
            index = 0
        }
        displayPlayGround(expectedText, index)
    }
}

async function generateText() {
    const response = await fetch("http://127.0.0.1:8081/tryphon/playground-text")
    return await response.text()
}

function displayPlayGround(text, index) {
    const typed = text.slice(0, index)
    const current = text[index]
    const toType = text.slice(index+1, text.length)
    document.getElementById('typed').innerHTML = typed.replaceAll(" ", "&nbsp;")
    document.getElementById('current').innerHTML = current.replaceAll(" ", "&nbsp;")
    document.getElementById('to-type').innerHTML = toType.replaceAll(" ", "&nbsp;")
}