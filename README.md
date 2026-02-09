# MegaCampaign Management Service

## API Endpoints

### Campaigns

#### List all campaigns
```
GET /campaigns
```
Public endpoint. Returns all campaigns with their configuration.

#### Create campaign
```
POST /campaigns?name=YourName
Authorization: Bearer <jwt>
```
Admin only. Creates a new campaign with the provided name. Returns the created campaign object with generated ID.

#### Update campaign
```
PATCH /campaigns/{id}
Authorization: Bearer <jwt>
Content-Type: application/json
```
Admin only. Updates specified fields of a campaign (partial update). Send only the fields you want to change.

#### Delete campaign
```
DELETE /campaigns/{id}
Authorization: Bearer <jwt>
```
Admin only. Deletes a campaign and all associated signups.

---

### Signups

#### Create signup
```
POST /campaigns/{campaignId}/signups
Content-Type: application/json

{
  "userId": "user123",
  "enteredBy": "user123",
  "preferenceKeys": ["key1", "key2", "key3"]
}
```
Public endpoint. Creates an immutable signup record. Rejects if campaign's `signupsOpen` is false.

#### Get signup count
```
GET /campaigns/{campaignId}/signups/count
```
Public endpoint. Returns the number of distinct users who have signed up for a campaign.

#### Get latest signups
```
GET /campaigns/{campaignId}/signups/latest
```
Public endpoint. Returns the most recent signup for each user in a campaign.

#### Get signup history (full audit trail)
```
GET /campaigns/{campaignId}/signups
Authorization: Bearer <jwt>
```
Admin only. Returns all signup submissions (full history) including timestamp and who entered each signup.

---

### Assignments

#### Update assignments (bulk replace)
```
PUT /campaigns/{campaignId}/assignments
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "assignments": [
    {
      "userId": "user123",
      "regionKey": "region_a"
    },
    {
      "userId": "user456",
      "regionKey": "region_b"
    }
  ]
}
```
Admin only. Atomically replaces all assignments for a campaign. Each user gets exactly one region, and each region is assigned to at most one user. Unassigned users and unassigned regions are acceptable (leave them out of the list).

#### Get assignments
```
GET /campaigns/{campaignId}/assignments
Authorization: Bearer <jwt>
```
Admin only. Returns all current assignments for a campaign.

---