import { Router, type IRouter } from "express";
import healthRouter from "./health";
import threatsRouter from "./threats";

const router: IRouter = Router();

router.use(healthRouter);
router.use(threatsRouter);

export default router;
