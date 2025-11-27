from datetime import date

def generate_current_period_keys() -> dict:
    """
    Genera los identificadores de período actuales para día, semana y mes.
    Ejemplo de salida:
    {
        "day": "2025-10-23",
        "week": "2025-W43",
        "month": "2025-10"
    }
    """
    today = date.today()

    # Día en formato YYYY-MM-DD
    day_key = today.strftime("%Y-%m-%d")

    # Semana ISO (YYYY-Www)
    year, week, _ = today.isocalendar()
    week_key = f"{year}-W{week:02d}"

    # Mes en formato YYYY-MM
    month_key = today.strftime("%Y-%m")

    return {
        "day": day_key,
        "week": week_key,
        "month": month_key
    }