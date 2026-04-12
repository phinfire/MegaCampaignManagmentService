# Mega Campaign Management Service

Spring Boot API for managing mega campaigns with signup assignments.

## API Endpoints

### Health
- `GET /health` - Service health check

### Campaigns
- `GET /campaigns` - List all campaigns  
  **Returns:** `List<MegaCampaign>`
- `POST /campaigns` - Create campaign (admin only)  
  **Returns:** `MegaCampaign`
- `PATCH /campaigns/{id}` - Update campaign (admin only)  
  **Input:** `MegaCampaignUpdate`  
  **Returns:** `MegaCampaign`
- `DELETE /campaigns/{id}` - Delete campaign (admin only)

### Signups
- `GET /campaigns/{campaignId}/signups/count` - Get unique signup count  
  **Returns:** `Long`
- `GET /campaigns/{campaignId}/signup/{userId}` - Get latest signup for user  
  **Returns:** `Signup`
- `GET /campaigns/{campaignId}/signups` - List latest signups for campaign  
  **Returns:** `List<SignupLatestView>`
- `GET /campaigns/{campaignId}/signups/{userId}` - Get latest signup for specific user  
  **Returns:** `SignupLatestView`
- `GET /campaigns/{campaignId}/signups/moderator/users` - List all signed-up user IDs (moderator only)  
  **Returns:** `List<String>`
- `POST /campaigns/{campaignId}/signups` - Create signup (authenticated)  
  **Input:** `SignupRequest`  
  **Returns:** `Signup`
- `POST /campaigns/{campaignId}/signups/{userId}` - Create signup for user (admin or self)  
  **Input:** `SignupRequest`  
  **Returns:** `Signup`
- `DELETE /campaigns/{campaignId}/signup/{userId}` - Delete signup (admin or self)

### Assignments
- `GET /campaigns/{campaignId}/assignments` - List assignments  
  **Returns:** `List<AssignmentView>`
- `PUT /campaigns/{campaignId}/assignments` - Batch update assignments (admin only)  
  **Input:** `AssignmentRequest`  
  **Returns:** `List<AssignmentView>`
- `POST /campaigns/{campaignId}/assignments/{userId}/{regionKey}` - Set assignment (admin only)  
  **Returns:** `AssignmentView`
- `DELETE /campaigns/{campaignId}/assignments/{userId}` - Delete assignment (admin only)

### Start Positions
- `GET /campaigns/{campaignId}/start-positions` - List all start positions  
  **Returns:** `List<MegaStartPositionView>`
- `GET /campaigns/{campaignId}/start-positions/{userId}` - Get start position for user  
  **Returns:** `MegaStartPositionView`
- `POST /campaigns/{campaignId}/start-positions/{userId}` - Set start position (self only)  
  **Input:** `MegaStartPositionRequest`  
  **Returns:** `MegaStartPositionView`
