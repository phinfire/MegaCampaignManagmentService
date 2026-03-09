## API Endpoints

### SignupController

- **POST /campaigns/{campaignId}/signups**
	- Headers: `Authorization: Bearer <token>`
	- Body:
		```json
		{
			"userId": "",
			"preferenceKeys": []
		}
		```
	- Creates a signup for a campaign.

- **GET /campaigns/{campaignId}/signups/count**
	- Returns the count of distinct users signed up for a campaign.

- **GET /campaigns/{campaignId}/signups/latest**
	- Returns the latest signup for each user in a campaign.

- **GET /campaigns/{campaignId}/signups**
	- Headers: `Authorization: Bearer <token>` (admin only)
	- Returns the full signup history for a campaign.

---

### AssignmentController

- **PUT /campaigns/{campaignId}/assignments**
	- Headers: `Authorization: Bearer <token>` (admin only)
	- Body:
		```json
		{
			"assignments": [
				{
					"userId": "",
					"regionKey": ""
				}
			]
		}
		```
	- Updates assignments for a campaign.

- **GET /campaigns/{campaignId}/assignments**
	- Headers: `Authorization: Bearer <token>` (admin only)
	- Returns all assignments for a campaign.

---

### MegaCampaignController

- **GET /campaigns**
	- Returns all campaigns.

- **POST /campaigns?name=...**
	- Headers: `Authorization: Bearer <token>` (admin only)
	- Creates a new campaign.

- **PATCH /campaigns/{id}**
	- Headers: `Authorization: Bearer <token>` (admin only)
	- Body:
		```json
		{
			"name": "",
			"signupsOpen": "",
			"signupDeadlineDate": "",
			"pickDeadline": "",
			"firstSessionDate": "",
			"firstEu4SessionDate": "",
			"moderatorIds": [],
			"ck3LobbiesIdentifiers": [],
			"eu4LobbiesIdentifiers": [],
			"vic3LobbyIdentifiers": [],
			"possibleKeys": [],
			"ck3MapGeoJsonUrl": "",
			"ck3RegionsConfigUrl": "",
			"nationsJsonUrl": ""
		}
		```
	- Updates campaign details.

- **DELETE /campaigns/{id}**
	- Headers: `Authorization: Bearer <token>` (admin only)
	- Deletes a campaign.

---

### MiscController

- **GET /health**
	- Returns status and timestamp for health check.

---

### Notes for Frontend Developers

- All endpoints requiring authentication expect an `Authorization` header with a valid JWT token.
- Request bodies should use the JSON structure shown above, with keys only (values are examples or left blank).
- For more details on response formats, see the corresponding DTO classes in the backend code.
- If you need a more formal API spec, consider generating an OpenAPI/Swagger document in the future.
