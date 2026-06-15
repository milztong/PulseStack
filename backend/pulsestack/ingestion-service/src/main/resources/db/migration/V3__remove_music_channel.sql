DELETE FROM news_items WHERE channel_id = (SELECT id FROM channels WHERE name = 'music');
DELETE FROM chat_messages WHERE channel_id = (SELECT id FROM channels WHERE name = 'music');
DELETE FROM channels WHERE name = 'music';
