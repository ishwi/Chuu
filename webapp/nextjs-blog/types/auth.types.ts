export interface IRegisterIn {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
}

export interface ILoginIn {
    email: string;
    password: string;
}

export interface IAuthInfo {
    email: string;
}
