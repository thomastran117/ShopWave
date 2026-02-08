import pytest
from playwright.async_api import expect

@pytest.mark.asyncio(loop_scope="session")
async def test_homepage(goto, page):
    await goto("/")
    await expect(page).to_have_url("http://localhost:3040/")
