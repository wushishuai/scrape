from playwright.sync_api import Playwright, sync_playwright


def run(playwright: Playwright) -> None:
    browser = playwright.firefox.launch(headless=False)
    context = browser.new_context()

    # Open new page
    page = context.new_page()

    # Go to https://www.google.com/search?client=firefox-b-d&q=%E8%B0%B7%E6%AD%8C
    page.goto("https://www.google.com/search?client=firefox-b-d&q=%E8%B0%B7%E6%AD%8C")

    # Click text=Google
    # with page.expect_navigation(url="https://www.google.co.jp/"):
    with page.expect_navigation():
        page.click("text=Google")

    # Click [aria-label="検索"]
    page.click("[aria-label=\"検索\"]")

    # Fill [aria-label="検索"]
    page.fill("[aria-label=\"検索\"]", "python")

    # Press Enter
    # with page.expect_navigation(url="https://www.google.co.jp/search?q=python&source=hp&ei=liDAYY2uIoXihwOI9aHgAQ&iflsig=ALs-wAMAAAAAYcAupopZxnJeTC_f3-1UInOFhHtKt-Lo&ved=0ahUKEwiN0YWK3vH0AhUF8WEKHYh6CBwQ4dUDCAg&uact=5&oq=python&gs_lcp=Cgdnd3Mtd2l6EAMyBQgAEIAEMggIABCABBCxAzIICAAQgAQQsQMyCAgAEIAEELEDMggIABCABBCxAzIICAAQgAQQsQMyCAgAEIAEELEDMggIABCABBCxAzoLCAAQgAQQsQMQgwE6DQgAEIAEELEDEIMBEARQlgdYjxFg0xhoAXAAeAGAAYMDiAH8DpIBBTItMi40mAEAoAEBsAEA&sclient=gws-wiz"):
    with page.expect_navigation():
        page.press("[aria-label=\"検索\"]", "Enter")
    # assert page.url == "https://www.google.co.jp/search?q=python&source=hp&ei=liDAYY2uIoXihwOI9aHgAQ&iflsig=ALs-wAMAAAAAYcAupopZxnJeTC_f3-1UInOFhHtKt-Lo&ved=0ahUKEwiN0YWK3vH0AhUF8WEKHYh6CBwQ4dUDCAg&uact=5&oq=python&gs_lcp=Cgdnd3Mtd2l6EAMyBQgAEIAEMggIABCABBCxAzIICAAQgAQQsQMyCAgAEIAEELEDMggIABCABBCxAzIICAAQgAQQsQMyCAgAEIAEELEDMggIABCABBCxAzoLCAAQgAQQsQMQgwE6DQgAEIAEELEDEIMBEARQlgdYjxFg0xhoAXAAeAGAAYMDiAH8DpIBBTItMi40mAEAoAEBsAEA&sclient=gws-wiz"

    # Click text=Python - Wikipedia
    # with page.expect_navigation(url="https://ja.wikipedia.org/wiki/Python"):
    with page.expect_navigation():
        page.click("text=Python - Wikipedia")

    # Click text=中文
    page.click("text=中文")
    # assert page.url == "https://zh.wikipedia.org/wiki/Python"

    # Close page
    page.close()

    # ---------------------
    context.close()
    browser.close()


with sync_playwright() as playwright:
    run(playwright)
