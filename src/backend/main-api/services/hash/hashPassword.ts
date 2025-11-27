import argon2 from "argon2";

async function hashPassword(plainPassword: string): Promise<string> {
    // Hashea la contraseña
    const hash = await argon2.hash(plainPassword, {
        type: argon2.argon2id,
        timeCost: 2,
        memoryCost: 2 ** 15,
        parallelism: 1,
    });
    return hash;
}

async function verifyPassword(
    plainPassword: string,
    hashedPassword: string,
): Promise<boolean> {
    try {
        const match = await argon2.verify(hashedPassword, plainPassword);
        return match;
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (err: unknown) {
        return false;
    }
}

export { hashPassword, verifyPassword };