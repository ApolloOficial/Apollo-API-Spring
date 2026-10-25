// Run manually with a provisioning user, after connecting to the intended cluster.
// Does not drop databases, collections, indexes, or existing events.
const analytics = db.getSiblingDB("apollo_analytics");
const uuid = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
const propertyNames = ["work_order_id", "maintenance_type_id", "action", "error_type", "input_method"];
const schema = {
  bsonType: "object",
  required: ["event_id", "event_name", "user_id", "company_id", "session_id", "screen",
    "timestamp", "received_at", "platform", "source", "schema_version", "properties"],
  additionalProperties: false,
  properties: {
    _id: { bsonType: "objectId" },
    event_id: { bsonType: "string", pattern: uuid },
    event_name: { enum: ["app_opened", "screen_viewed", "work_order_viewed",
      "work_order_start_clicked", "work_order_started", "barcode_scanned", "report_started",
      "report_submitted", "work_order_completed", "action_failed"] },
    user_id: { bsonType: "string", pattern: uuid },
    company_id: { bsonType: ["long", "int"] },
    session_id: { bsonType: "string", pattern: uuid },
    screen: { bsonType: "string", pattern: "^[A-Za-z][A-Za-z0-9_]{0,79}$" },
    timestamp: { bsonType: "date" },
    received_at: { bsonType: "date" },
    platform: { enum: ["android", "api"] },
    source: { enum: ["android", "api"] },
    schema_version: { bsonType: "int", enum: [1] },
    properties: {
      bsonType: "object", maxProperties: 8, additionalProperties: false,
      properties: Object.fromEntries(propertyNames.map(name => [name,
        { bsonType: "string", maxLength: 128, pattern: "^[A-Za-z0-9_.:/ -]*$" }]))
    }
  }
};

const validator = { $jsonSchema: schema };
if (analytics.getCollectionInfos({ name: "tracking_events" }).length === 0) {
  analytics.createCollection("tracking_events", {
    validator, validationLevel: "strict", validationAction: "error"
  });
} else {
  const result = analytics.runCommand({ collMod: "tracking_events", validator,
    validationLevel: "strict", validationAction: "error" });
  if (!result.ok) throw new Error("Cannot configure tracking validator");
}

analytics.tracking_events.createIndex({ event_id: 1 }, {
  unique: true, name: "ux_tracking_event_id"
});
analytics.tracking_events.createIndex({ received_at: 1, event_id: 1 }, {
  name: "ix_tracking_incremental"
});
printjson({ database: analytics.getName(), collection: "tracking_events",
  indexes: analytics.tracking_events.getIndexes() });
