type createAgencyResponse = ApiResponse & {
    agency: Agency;
};
type assignAgencyAdminResponse = ApiResponse;

type GetAgenciesResponse = ApiResponse & {
    agencies: Agency[];
};

type VerifyIfUserIsAgencyAdminResponse = ApiResponse & {
    isAdmin: boolean;
};