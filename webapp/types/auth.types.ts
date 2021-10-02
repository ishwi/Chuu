export interface IRegisterIn {
    lastfm_id: string;
    discord_name: string;
    discord_id: string;
    roles: string[];
}

export interface ILoginIn {
    email: string;
    password: string;
}

export interface OAuthOkay {
    accessToken: string;
    expiresInd: number;
    roles: string[];
    token_type: 'bearer';
    username: string;
}
