import { Request, Response } from 'express';
import { NextFunction } from "express";

type AppRequest = session & Request;

type AppResponse = Response;

type AppNext = NextFunction;