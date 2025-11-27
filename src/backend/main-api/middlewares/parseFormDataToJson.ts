import { Request, Response, NextFunction } from "express";

/**
 * Middleware que intenta parsear campos JSON de form-data.
 * @param fieldsToParse Lista de campos que se intentarán parsear.
 */
export function parseFormDataToJson(fieldsToParse: string[]) {
  return (req: Request, res: Response, next: NextFunction): void => {
    try {
      fieldsToParse.forEach((field) => {
        const value = req.body[field];
        if (typeof value === "string") {
          try {
            req.body[field] = JSON.parse(value);
          } catch {
            // Deja el valor como string
          }
        }
      });
      next();
    } catch (err) {
      res.status(400).json({
        ok: false,
        message: "Error procesando los datos del formulario.",
        error: (err as Error).message,
      });
    }
  };
}
