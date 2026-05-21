import json
from locust import HttpUser, task, between

class BidMartOrderNotificationUser(HttpUser):
    wait_time = between(0.1, 1.0) # Simulate quick successive bids and auction completions

    @task(3)
    def test_get_notifications(self):
        # Simulate users checking their real-time notifications
        self.client.get("/api/order-notification/notifications/user123", name="Get Notifications")

    @task(2)
    def test_get_preferences(self):
        # Simulate users fetching notification preferences
        self.client.get("/api/order-notification/preferences/user123", name="Get Preferences")

    @task(1)
    def test_post_auction_finish_sync(self):
        # Simulate the direct HTTP REST call for order notification creation (synchronous mode)
        payload = {
            "auctionId": 9999,
            "winnerId": "user123",
            "itemName": "Premium Gold Bar",
            "finalPrice": 15000000.0
        }
        headers = {'Content-Type': 'application/json'}
        self.client.post(
            "/api/order-notification/auction-finish",
            data=json.dumps(payload),
            headers=headers,
            name="Post Auction Finish (Sync REST)"
        )
