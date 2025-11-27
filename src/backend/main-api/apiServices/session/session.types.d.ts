type UserWithPassword = {
    user: User,
    password: string;
};

type LoginDTO = {
    user: User,
    refreshToken: string,
    accessToken: string,   
}

type LoginResponse = ApiResponse & {
    user: User,
    refreshToken: string,
    accessToken: string,   
}

type refreshSessionTokenResponse = ApiResponse & {
    accessToken: string,
}

type CreateDeviceTokenResponse = ApiResponse & {
    deviceToken: string,
}
