import imaplib
import email
from email.header import decode_header
import time
import logging
from datetime import datetime

# ─────────────────────────────────────────
#  НАЛАШТУВАННЯ
# ─────────────────────────────────────────
IMAP_SERVER = "imap.gmail.com"
IMAP_PORT   = 993
EMAIL       = "email@gmail.com"
PASSWORD    = "your password"
CHECK_EVERY = 30
MAILBOX     = "INBOX"
# ─────────────────────────────────────────

# Налаштування логування
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)s  %(message)s",
    handlers=[
        logging.FileHandler("emails.log", encoding="utf-8"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

def decode_str(value: str) -> str:
    """Декодує заголовок листа з обробкою помилок невідомих кодувань."""
    if not value:
        return "(no value)"
    
    try:
        parts = decode_header(value)
        result = []
        for part, charset in parts:
            if isinstance(part, bytes):
                # Якщо charset невідомий (None або unknown-8bit), використовуємо utf-8 з заміною помилок
                try:
                    encoding = charset if charset and charset != 'unknown-8bit' else 'utf-8'
                    result.append(part.decode(encoding, errors="replace"))
                except:
                    result.append(part.decode('utf-8', errors="replace"))
            else:
                result.append(str(part))
        return "".join(result)
    except Exception:
        return str(value)

def fetch_unseen(mail: imaplib.IMAP4_SSL) -> list[dict]:
    """Повертає список нових (UNSEEN) листів."""
    mail.select(MAILBOX)
    # Шукаємо всі листи. Можна змінити "UNSEEN" на "ALL", якщо хочеш побачити взагалі всі листи
    status, data = mail.search(None, "UNSEEN")
    if status != "OK" or not data[0]:
        return []

    messages = []
    # Беремо останні 5 повідомлень, щоб не перевантажувати термінал
    ids = data[0].split()
    for num in ids[-5:]: 
        status, msg_data = mail.fetch(num, "(RFC822)")
        if status != "OK":
            continue

        raw = msg_data[0][1]
        msg = email.message_from_bytes(raw)

        sender  = decode_str(msg.get("From", "Unknown"))
        subject = decode_str(msg.get("Subject", "(no subject)"))

        messages.append({"from": sender, "subject": subject})

    return messages

def check_inbox() -> None:
    """Підключається до IMAP і перевіряє нові листи."""
    try:
        mail = imaplib.IMAP4_SSL(IMAP_SERVER, IMAP_PORT)
        mail.login(EMAIL, PASSWORD)

        new_emails = fetch_unseen(mail)

        if new_emails:
            logger.info("=== Знайдено листи (останні %d) ===", len(new_emails))
            for i, em in enumerate(new_emails, 1):
                logger.info("  [%d] Від   : %s", i, em["from"])
                logger.info("  [%d] Тема  : %s", i, em["subject"])
        else:
            logger.info("Нових листів немає.")

        mail.logout()

    except imaplib.IMAP4.error as e:
        logger.error("IMAP помилка (перевірте пароль або доступ): %s", e)
    except Exception as e:
        logger.error("Помилка при читанні: %s", e)

def main() -> None:
    logger.info("Email receiver запущено. Перевірка кожні %d сек.", CHECK_EVERY)
    while True:
        check_inbox()
        # Для лаби краще вимкнути нескінченний цикл або зробити паузу
        logger.info("Чекаємо наступної перевірки...")
        time.sleep(CHECK_EVERY)

if __name__ == "__main__":
    main()
