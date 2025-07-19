UPDATE emotion
SET disgust = COALESCE(disgust,0) + COALESCE(neutral,0);
ALTER TABLE emotion DROP COLUMN neutral;