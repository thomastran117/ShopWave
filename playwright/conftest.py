import os
import pytest
from playwright.async_api import async_playwright

BASE_URL = os.getenv("BASE_URL", "http://localhost:3040")

@pytest.fixture(scope="session")
async def playwright_instance():
    async with async_playwright() as p:
        yield p

@pytest.fixture(scope="session")
async def browser(playwright_instance):
    browser = await playwright_instance.chromium.launch(headless=True)
    yield browser
    await browser.close()

@pytest.fixture
async def context(browser):
    context = await browser.new_context()
    yield context
    await context.close()

@pytest.fixture
async def page(context):
    page = await context.new_page()
    yield page
    await page.close()

@pytest.fixture
async def goto(page):
    async def _goto(path: str = "/"):
        page.set_default_navigation_timeout(10_000)
        page.set_default_timeout(10_000)
        await page.goto(f"{BASE_URL}{path}", wait_until="domcontentloaded")
    return _goto
